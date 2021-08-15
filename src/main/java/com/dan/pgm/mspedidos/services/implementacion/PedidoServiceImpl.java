package com.dan.pgm.mspedidos.services.implementacion;

import com.dan.pgm.mspedidos.dao.PedidoRepositoryH2;
import com.dan.pgm.mspedidos.database.PedidoRepository;
import com.dan.pgm.mspedidos.domain.*;
import com.dan.pgm.mspedidos.dtos.DetallePedidoDTO;
import com.dan.pgm.mspedidos.dtos.PedidoDTO;
import com.dan.pgm.mspedidos.services.ClienteService;
import com.dan.pgm.mspedidos.services.MaterialService;
import com.dan.pgm.mspedidos.services.PedidoService;
import org.apache.commons.collections.ArrayStack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final String GET_OBRA = "/{id}";
    private static final String GET_IDS_OBRAS = "/by-idcliente-cuit";
    private static final String REST_API_OBRA_URL = "http://localhost:8080/api/obra";
    private static final String GET_STOCK_PRODUCTO = "/verificar-stock";
    private static final String REST_API_PRODUCTO_URL = "http://localhost:9001/api/producto";

    @Autowired
    MaterialService materialSrv;

    @Autowired
    PedidoRepositoryH2 repoPedido;

    @Autowired
    PedidoRepository pedidoRepository;

    @Autowired
    ClienteService clienteSrv;

    @Autowired
    JmsTemplate jms;


    @Override
    public Pedido crearPedido(Pedido p) {
        this.pedidoRepository.save(p);
         return enviarPedidoACorralon(p.getId());
    }

    public Pedido enviarPedidoACorralon(Integer pedidoId) {
        Pedido p = new Pedido();
        try {
            if (this.pedidoRepository.findById(pedidoId).isPresent()) {
                p = this.pedidoRepository.findById(pedidoId).get();
            } else {
                throw new RuntimeException("No se halló el pedido con id: "+pedidoId);
            }
        } catch(Exception exception) {
            System.out.println(exception.getMessage());
        }

        boolean hayStock = p.getDetalle()
                .stream()
                .allMatch(dp -> verificarStock(dp.getProducto(),dp.getCantidad()));

        Double totalOrden = p.getDetalle()
                .stream()
                .mapToDouble( dp -> dp.getCantidad() * dp.getPrecio())
                .sum();

        // TODO IMPLEMENTAR DEUDA CLIENTE
        Double saldoCliente = clienteSrv.deudaCliente(p.getObra());
        Double nuevoSaldo = saldoCliente - totalOrden;

        Boolean generaDeuda= nuevoSaldo<0;
        if(hayStock) {
            if(!generaDeuda || (generaDeuda && this.esDeBajoRiesgo(p.getObra(),nuevoSaldo) ))  {
                p.setEstado(EstadoPedido.ACEPTADO);
                PedidoDTO pedidoAEnviar = new PedidoDTO();
                pedidoAEnviar.setId(p.getId());
                List<DetallePedidoDTO> detallePedidoAEnviar = new ArrayList<DetallePedidoDTO>();
                p.getDetalle().stream().forEach(detalle -> {
                    DetallePedidoDTO detalleDTO = new DetallePedidoDTO();
                    detalleDTO.setCantidad(detalle.getCantidad());
                    detalleDTO.setPrecio(detalle.getPrecio());
                    detalleDTO.setProductoId(detalle.getProducto().getId().toString());
                    detallePedidoAEnviar.add(detalleDTO);
                });
                pedidoAEnviar.setDetalle(detallePedidoAEnviar);
                jms.convertAndSend("COLA_PEDIDOS", "PedidoDTO:" + pedidoAEnviar);
            } else {
                p.setEstado(EstadoPedido.RECHAZADO);
                throw new RuntimeException("No tiene aprobacion crediticia");
            }
        } else {
            p.setEstado(EstadoPedido.PENDIENTE);
        }
        return this.pedidoRepository.save(p);
    }

    @Override
    public Pedido agregarDetallePedido(Integer idPedido, DetallePedido detallePedido) {
        Pedido p = buscarPedidoPorId(idPedido);
        if( p != null){
            p.getDetalle().add(detallePedido);
            return pedidoRepository.save(p);
        } else {
            return null;
        }
    }

    //TODO validar campos en la UI
    @Override
    public Pedido actualizarPedido(Pedido pedido, Integer idPedido) {
        return pedidoRepository.save(pedido);
    }

    public String actualizarEstado(Integer idPedido, String estado){
        Pedido pedido = this.buscarPedidoPorId(idPedido);
        if(pedido!=null){
            if(estado.toUpperCase(Locale.ROOT).equals("CONFIRMADO")){
                Double sumaPrecio = 0.0;

                for(DetallePedido dp: pedido.getDetalle()){

                    String url = REST_API_PRODUCTO_URL + GET_STOCK_PRODUCTO + "/"+dp.getProducto().getId();
                    WebClient client = WebClient.create(url);

                    Boolean hayStock = client.get()
                            .uri(url).accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block();

                    //Sumar precio de cada DetallePedido
                    sumaPrecio+= dp.getPrecio();

                    if(!hayStock){
                        pedido.setEstado(EstadoPedido.PENDIENTE);
                        repoPedido.save(pedido);
                        return "Estado final: PENDIENTE";
                    }else{

                        //TODO SEGUIR
                        return "SEGUIR";
                    }

                }
            }
        }else{
            throw new RuntimeException("El pedido con id: " + idPedido + " No fue encontrado");
        }
        return "SEGUIR"; //TODO SEGUIR
    }

    //TODO HACER CUANDO ESTE EL MICROSERVICIO CUENTACORRIENTE
    /*
    b) El pedido no genera saldo deudor
    c) Si el pedido genera saldo deudor se verifica que se cumpla que
        i. Que el saldo deudor sea menor que el descubierto
        ii. Que la situación crediticia en BCRA sea de bajo riesgo.
    d) Si se cumple la condición a y al menos una de las condiciones b o c el pedido es cargado como ACEPTADO
    e) Si no se cumple “a” es cargado como PENDIENTE.
    f) Si no se cumple b ni c, se rechaza el pedido y se lanza una excepción
     */

    @Override
    public boolean borrarPedido(Integer idPedido) {
        Pedido p = buscarPedidoPorId(idPedido);
        if( p != null){
            pedidoRepository.delete(p);

            if (pedidoRepository.findById(idPedido).isPresent()) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }


    }

    @Override
    public boolean borrarDetalleDePedido(Integer idPedido, Integer idDetalle) {
        Pedido p = buscarPedidoPorId(idPedido);
        if( p != null){
            List<DetallePedido> nuevosDetalles = p.getDetalle().stream().filter(detalle -> detalle.getId() != idDetalle).collect(Collectors.toList());
            p.setDetalle(nuevosDetalles);
            pedidoRepository.save(p);

            DetallePedido detPedido = buscarDetallePorId(idPedido, idDetalle);
            if(detPedido != null) {
                return false;
            }
            return true;
        } else {
            return false;
        }

    }
    //TODO validar campos en la UI
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
            if (pedidoRepository.findById(idPedido).isPresent()) {
                return pedidoRepository.findById(idPedido).get();
            } else {
            throw new RuntimeException("No se halló el pedido con id: " + idPedido);
            }
        } catch ( Exception exception){
            System.out.println(exception.getMessage());
            return null;
        }
    }

    @Override
    public List<Pedido> buscarPedidoPorIdObra(Integer idObra) {
        try{
            if(pedidoRepository.findByObraId(idObra).isPresent()){
                return pedidoRepository.findByObraId(idObra).get();
            } else {
                throw new RuntimeException("No se halló el pedido con idObra " + idObra);
            }
        } catch ( Exception exception){
            System.out.println(exception.getMessage());
            return null;
        }
    }

    @Override
    public List<Pedido> buscarPedidoPorEstado(String estado) {
       List<Pedido> pedidos = pedidoRepository.findByEstado(estado);
        try{
            if(pedidos.size() > 0){
                return pedidos;
            } else {
                throw new RuntimeException("No se encuentran pedidos en el estado: " + estado);
            }
        } catch ( Exception exception){
            System.out.println(exception.getMessage());
            return null;
        }
    }

    @Override
    public List<Pedido> pedidoPorIdClienteCuit(Integer idCliente, String cuit) {
        List<Pedido> pedidosFiltrados = new ArrayList<>();
        String finalURL = "?";
        if (idCliente != null) {
            finalURL += "idCliente=" + idCliente;
        }
        if (cuit != null) {
            finalURL += "cuit=" + cuit;
        }
        List<Integer> listaIdsObras = getIdsObras(finalURL);

        listaIdsObras.forEach( id -> pedidosFiltrados.addAll(buscarPedidoPorIdObra(id)));

        return pedidosFiltrados;
    }


    @Override
    public DetallePedido buscarDetallePorId(Integer idPedido, Integer idDetalle) {
        Pedido pedido = buscarPedidoPorId(idPedido);
        if(pedido != null){
            DetallePedido detalle = pedido.getDetalle().stream().filter(det -> det.getId() == idDetalle).findFirst().get();
            return detalle;
        }else{
            return null;
        }
    }




    @Override
    public boolean verificarExistenciaDePedidos(ArrayList<Integer> idsDeObras) {
        List<Pedido> pedidosFiltrados = new ArrayList<>();
        idsDeObras.forEach( id -> pedidosFiltrados.addAll(buscarPedidoPorIdObra(id)));

        if(pedidosFiltrados.size() > 0) {
            return true;
        }

        return false;
    }


    public boolean verificarStock(Producto p, Integer cantidad) {

        return materialSrv.stockDisponible(p)>=cantidad;
    }

    public boolean esDeBajoRiesgo(Obra o, Double saldoNuevo) {
        Double maximoSaldoNegativo = clienteSrv.maximoSaldoNegativo(o);
        Boolean tieneSaldo = Math.abs(saldoNuevo) < maximoSaldoNegativo;
        return tieneSaldo;
    }

    public List<Integer> getIdsObras(String finalURL) {
        String url = REST_API_OBRA_URL + GET_IDS_OBRAS + finalURL;
        WebClient client = WebClient.create(url);

        return client.get()
                .uri(url).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Integer.class)
                .collectList()
                .block();
    }

}