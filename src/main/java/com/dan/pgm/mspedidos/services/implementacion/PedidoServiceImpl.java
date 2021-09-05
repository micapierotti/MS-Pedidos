package com.dan.pgm.mspedidos.services.implementacion;

import com.dan.pgm.mspedidos.database.DetallePedidoRepository;
import com.dan.pgm.mspedidos.database.PedidoRepository;
import com.dan.pgm.mspedidos.domain.*;
import com.dan.pgm.mspedidos.dtos.DetallePedidoDTO;
import com.dan.pgm.mspedidos.dtos.ObraDTO;
import com.dan.pgm.mspedidos.dtos.PedidoDTO;
import com.dan.pgm.mspedidos.services.ClienteService;
import com.dan.pgm.mspedidos.services.MaterialService;
import com.dan.pgm.mspedidos.services.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final String REST_API_OBRA_URL = "http://localhost:9000/api/obra/";

    @Autowired
    CircuitBreakerFactory circuitBreakerFactory;

    @Autowired
    MaterialService materialSrv;

    @Autowired
    PedidoRepository pedidoRepository;

    @Autowired
    DetallePedidoRepository detallePedidoRepository;

    @Autowired
    ClienteService clienteSrv;

    @Autowired
    JmsTemplate jms;


    @Override
    public Pedido crearPedido(Pedido p) {
        this.pedidoRepository.save(p);
        return enviarPedidoACorralon(p.getId());
    }

    private Pedido enviarPedidoACorralon(Integer pedidoId) {
        Pedido p = new Pedido();

        try {
            if (this.pedidoRepository.findById(pedidoId).isPresent())
                p = this.pedidoRepository.findById(pedidoId).get();
            else throw new RuntimeException("No se halló el pedido con id: "+pedidoId);
        } catch(Exception exception) {
            System.out.println(exception.getMessage());
        }

        System.out.println("Pedido encontrado : "+p.getId());

        boolean hayStock = true;
        for(DetallePedido dp : p.getDetalle()){
            if(!verificarStock(dp.getIdProducto(), dp.getCantidad()))
                hayStock = false;
        }

        Double totalOrden = p.getDetalle()
                .stream()
                .mapToDouble( dp -> dp.getCantidad() * dp.getPrecio())
                .sum();

        Double saldoCliente = clienteSrv.deudaCliente(p.getIdObra());
        boolean generaDeuda = (saldoCliente - totalOrden) < 0;

        System.out.println("Total orden: "+totalOrden);
        System.out.println("Saldo cliente: "+saldoCliente);
        System.out.println("Genera deuda: "+generaDeuda);
        System.out.println("Tipo 1: "+this.esDeBajoRiesgo(p.getIdObra()));

        if(hayStock) {
            if(!generaDeuda || this.esDeBajoRiesgo(p.getIdObra()))  {
                p.setEstado(EstadoPedido.ACEPTADO);

                List<Integer> idsDetalles = new ArrayList<>();

                for(DetallePedido det : p.getDetalle())
                    idsDetalles.add(det.getId());

                System.out.println("Ids a enviar: "+idsDetalles);
                jms.convertAndSend("COLA_PEDIDOS", idsDetalles);
            } else {
                p.setEstado(EstadoPedido.RECHAZADO);
                throw new RuntimeException("No tiene aprobación crediticia: el pedido genera saldo deudor mayor al descubierto y la situación crediticia en BCRA no es de bajo riesgo.");
            }
        } else {
            p.setEstado(EstadoPedido.PENDIENTE);
        }
        return this.pedidoRepository.save(p);
    }


    @Override
    public Pedido agregarDetallePedido(Integer idPedido, DetallePedido detallePedido) {

        Pedido p = buscarPedidoPorId(idPedido);

        if(p != null){
            List<DetallePedido> newDetalle = p.getDetalle();

            newDetalle.add(detallePedido);
            p.setDetalle(newDetalle);

            return pedidoRepository.save(p);
        } else {
            return null;
        }
    }


    @Override
    public Pedido actualizarPedido(Pedido pedido, Integer idPedido) {
        return pedidoRepository.save(pedido);
    }

    public String actualizarEstado(Integer idPedido, String estado){

        Pedido pedido = this.buscarPedidoPorId(idPedido);

        if(pedido!=null){
            if(estado.toUpperCase(Locale.ROOT).equals("CONFIRMADO")){

                boolean hayStock = true;
                for(DetallePedido dp : pedido.getDetalle()){
                    if(!verificarStock(dp.getIdProducto(), dp.getCantidad()))
                        hayStock = false;
                }

                Double totalOrden = pedido.getDetalle()
                        .stream()
                        .mapToDouble( dp -> dp.getCantidad() * dp.getPrecio())
                        .sum();

                Double saldoCliente = clienteSrv.deudaCliente(pedido.getIdObra());
                boolean generaDeuda = (saldoCliente - totalOrden) < 0;

                System.out.println("Total orden: "+totalOrden);
                System.out.println("Saldo cliente: "+saldoCliente);

                if(hayStock) {
                    if(!generaDeuda || this.esDeBajoRiesgo(pedido.getIdObra()))  {
                        pedido.setEstado(EstadoPedido.ACEPTADO);
                    } else {
                        pedido.setEstado(EstadoPedido.RECHAZADO);
                        throw new RuntimeException("No tiene aprobación crediticia: el pedido genera saldo deudor mayor al descubierto y la situación crediticia en BCRA no es de bajo riesgo.");
                    }
                } else {
                    pedido.setEstado(EstadoPedido.PENDIENTE);
                }
            }else{
                EstadoPedido state = EstadoPedido.valueOf(estado.toUpperCase(Locale.ROOT));
                pedido.setEstado(state);
            }
            pedidoRepository.save(pedido);
            return "El estado se ha actualizado correctamente a "+pedido.getEstado();
        }
        throw new RuntimeException("El pedido con id: "+idPedido+" no fue encontrado");
    }


    @Override
    public boolean borrarPedido(Integer idPedido) {

        Pedido p = buscarPedidoPorId(idPedido);

        if(p != null){
            p.getDetalle().forEach(detallePedido -> borrarDetalleDePedido(idPedido, detallePedido.getId()));
            pedidoRepository.delete(p);

            return !pedidoRepository.findById(idPedido).isPresent();
        } else {
            return false;
        }
    }


    @Override
    public boolean borrarDetalleDePedido(Integer idPedido, Integer idDetalle) {

        Pedido p = buscarPedidoPorId(idPedido);

        if(p != null){
            List<DetallePedido> nuevosDetalles = p.getDetalle().stream().filter(detalle -> !detalle.getId().equals(idDetalle)).collect(Collectors.toList());
            p.setDetalle(nuevosDetalles);
            pedidoRepository.save(p);

            if(detallePedidoRepository.findById(idDetalle).isPresent())
                detallePedidoRepository.delete(detallePedidoRepository.findById(idDetalle).get());

            return !detallePedidoRepository.findById(idDetalle).isPresent();
        } else {
            return false;
        }

    }


    @Override
    public Pedido actualizarDetallePedido(List<DetallePedido> detalles, Integer idPedido) {

        Pedido p = buscarPedidoPorId(idPedido);

        if(p != null){
            p.setDetalle(detalles);
            return pedidoRepository.save(p);
        } else {
            return null;
        }
    }


    @Override
    public Pedido buscarPedidoPorId(Integer idPedido) {
        try{

            if (pedidoRepository.findById(idPedido).isPresent())
                return pedidoRepository.findById(idPedido).get();
            else
                throw new RuntimeException("No se halló el pedido con id: " + idPedido);

        } catch ( Exception exception){
            System.out.println(exception.getMessage());
            return null;
        }
    }


    @Override
    public List<Pedido> buscarPedidoPorIdObra(Integer idObra) {
        try{

            if(pedidoRepository.findByIdObra(idObra).isPresent())
                return pedidoRepository.findByIdObra(idObra).get();
            else
                throw new RuntimeException("No se halló el pedido con idObra " + idObra);

        } catch (Exception exception){
            System.out.println(exception.getMessage());
            return null;
        }
    }


    @Override
    public List<Pedido> buscarPedidoPorEstado(String estado) {

        EstadoPedido state = EstadoPedido.valueOf(estado.toUpperCase(Locale.ROOT));
        System.out.println("ESTADO: ... "+state+" - "+estado);
        List<Pedido> pedidos = pedidoRepository.findByEstado(state);

        try{
            if(pedidos.size() > 0)
                return pedidos;
            else
                throw new RuntimeException("No se encuentran pedidos en el estado: " + estado);
        } catch ( Exception exception){
            System.out.println(exception.getMessage());
            return null;
        }
    }


    @Override
    public List<Pedido> pedidoPorIdCliente(Integer idCliente) {
        List<Pedido> pedidosFiltrados = new ArrayList<>();
        String finalURL = "?idCliente=" + idCliente;

        List<Integer> listaIdsObras = getIdsObras(finalURL);
        listaIdsObras.forEach(id -> pedidosFiltrados.addAll(buscarPedidoPorIdObra(id)));

        return pedidosFiltrados;
    }


    private List<Integer> getIdsObras(String finalURL) {

        List<Integer> idsObras = new ArrayList<>();
        String url = REST_API_OBRA_URL + finalURL;
        WebClient client = WebClient.create(url);
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");

        return circuitBreaker.run(() -> {
            try{
                List<ObraDTO> obrasResult= client.get()
                        .uri(url).accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToFlux(ObraDTO.class)
                        .collectList()
                        .block();

                obrasResult.forEach(obra -> idsObras.add(obra.getId()));

                return idsObras;
            } catch (Exception e){
                return idsObras;
            }
        }, throwable -> defaultIdsObras());
    }

    private List<Integer> defaultIdsObras() {
        return new ArrayList<Integer>();
    }

    @Override
    public DetallePedido buscarDetallePorId(Integer idPedido, Integer idDetalle) {

        Pedido pedido = buscarPedidoPorId(idPedido);

        if(pedido != null){

            if(pedido.getDetalle().stream().anyMatch(det -> det.getId().equals(idDetalle)))
                return pedido.getDetalle().stream().filter(det -> det.getId().equals(idDetalle)).findFirst().get();
            else
                return null;

        }else
            throw new RuntimeException("No se halló el pedido con id: "+idPedido);
    }

    @Override
    public DetallePedido buscarDetallePorSuId(Integer idDetalle) {
        if( detallePedidoRepository.findById(idDetalle).isPresent())
            return detallePedidoRepository.findById(idDetalle).get();
        else
            return null;
    }


    @Override
    public boolean verificarExistenciaDePedidos(ArrayList<Integer> idsDeObras) {
        List<Pedido> pedidosFiltrados = new ArrayList<>();
        idsDeObras.forEach( id -> pedidosFiltrados.addAll(buscarPedidoPorIdObra(id)));

        return pedidosFiltrados.size() > 0;
    }


    private boolean verificarStock(Integer idProducto, Integer cantidad) {
        System.out.println(materialSrv.stockDisponible(idProducto)+" > "+cantidad+"? -> "+(materialSrv.stockDisponible(idProducto)>=cantidad));
        return materialSrv.stockDisponible(idProducto)>=cantidad;
    }


    private boolean esDeBajoRiesgo(Integer idObra) {
        return clienteSrv.situacionCrediticiaBCRA(idObra) == 1;
    }


    @Override
    public boolean existeObra(Integer idObra) {
        String url = REST_API_OBRA_URL + idObra;
        WebClient client = WebClient.create(url);
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");

        return circuitBreaker.run(() -> {
            try{
                ObraDTO obraResult= client.get()
                        .uri(url).accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(ObraDTO.class)
                        .block();
                return true;
            } catch (Exception e){
                return false;
            }
        }, throwable -> defaultExisteObra());
    }

    private boolean defaultExisteObra() {
        return false;
    }

    /////PARA MICROSERVICIO CUENTA CORRIENTE
    @Override
    public List<PedidoDTO> facturasPorClienteId(Integer idCliente) {
        List<Pedido> facturasPorCliente = new ArrayList<>();
        List<PedidoDTO> facturasPorClienteDTO = new ArrayList<>();
        String finalURL = "?idCliente=" + idCliente;

        List<Integer> listaIdsObras = getIdsObras(finalURL);

        listaIdsObras.forEach( id -> facturasPorCliente.addAll(buscarPedidoPorIdObra(id)));


        facturasPorClienteDTO = facturasPorCliente.stream()
                .map(f -> new PedidoDTO(f.getId(),
                        f.getDetalle().stream()
                                .map( dp -> new DetallePedidoDTO(dp.getId(), dp.getIdProducto(), dp.getCantidad(), dp.getPrecio()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());

        return facturasPorClienteDTO;
    }
}