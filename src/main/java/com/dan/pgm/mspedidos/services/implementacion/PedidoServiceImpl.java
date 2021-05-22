package com.dan.pgm.mspedidos.services.implementacion;

import com.dan.pgm.mspedidos.dao.PedidoRepository;
import com.dan.pgm.mspedidos.domain.*;
import com.dan.pgm.mspedidos.dtos.ObraDTO;
import com.dan.pgm.mspedidos.services.ClienteService;
import com.dan.pgm.mspedidos.services.MaterialService;
import com.dan.pgm.mspedidos.services.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final String GET_OBRA = "/{id}";
    private static final String GET_IDS_OBRAS = "/by-idcliente-cuit";
    private static final String REST_API_URL = "http://localhost:8080/api/obra";

    @Autowired
    MaterialService materialSrv;

    @Autowired
    PedidoRepository repoPedido;

    @Autowired
    ClienteService clienteSrv;


    @Override
    public Pedido crearPedido(Pedido p) {
        System.out.println("HOLA PEDIDO "+p);
        boolean hayStock = p.getDetalle()
                .stream()
                .allMatch(dp -> verificarStock(dp.getProducto(),dp.getCantidad()));

        Double totalOrden = p.getDetalle()
                .stream()
                .mapToDouble( dp -> dp.getCantidad() * dp.getPrecio())
                .sum();


        Double saldoCliente = clienteSrv.deudaCliente(p.getObra());
        Double nuevoSaldo = saldoCliente - totalOrden;

        Boolean generaDeuda= nuevoSaldo<0;
        if(hayStock ) {
            if(!generaDeuda || (generaDeuda && this.esDeBajoRiesgo(p.getObra(),nuevoSaldo) ))  {
                p.setEstado(new EstadoPedido(1,"ACEPTADO"));
            } else {
                throw new RuntimeException("No tiene aprobacion crediticia");
            }
        } else {
            p.setEstado(new EstadoPedido(2,"PENDIENTE"));
        }
        return this.repoPedido.save(p);
    }

    @Override
    public Pedido agregarDetallePedido(Integer idPedido, DetallePedido detallePedido) {
        Pedido pedido = repoPedido.findById(idPedido).get();
        pedido.getDetalle().add(detallePedido);
        repoPedido.save(pedido);
        return pedido;
    }

    //TODO validar campos en la UI
    @Override
    public Pedido actualizarPedido(Pedido pedido, Integer idPedido) {
        repoPedido.save(pedido);
        return pedido;
    }

    @Override
    public boolean borrarPedido(Integer id) {
        Pedido pedido = repoPedido.findById(id).get();
        repoPedido.delete(pedido);
        if (repoPedido.findByObraId(id).isPresent()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean borrarDetalleDePedido(Integer id, Integer idDetalle) {
        Pedido pedido = repoPedido.findById(id).get();
        List<DetallePedido> nuevosDetalles = pedido.getDetalle().stream().filter(detalle -> detalle.getId() != idDetalle).collect(Collectors.toList());
        pedido.setDetalle(nuevosDetalles);
        repoPedido.save(pedido);
        DetallePedido detPedido = buscarDetallePorId(id, idDetalle);
        if(detPedido != null) {
            return false;
        }

        return true;
    }

    @Override
    public Optional<Pedido> buscarPedidoPorId(Integer id) {
        return repoPedido.findById(id);
    }

    @Override
    public Optional<Pedido> buscarPedidoPorIdObra(Integer id) {
        return repoPedido.findByObraId(id);
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

        listaIdsObras.forEach( id -> pedidosFiltrados.add(buscarPedidoPorIdObra(id).get()));

        return pedidosFiltrados;
    }

    //TODO REPOSITORIO DE DETALLE PEDIDO
    @Override
    public DetallePedido buscarDetallePorId(Integer idPedido, Integer idDetalle) {
        Pedido pedido = repoPedido.findById(idPedido).get();
        DetallePedido detalle = pedido.getDetalle().stream().filter(det -> det.getId() == idDetalle).findFirst().get();
        return detalle;
    }


    @Override
    public boolean verificarExistenciaDePedidos(ArrayList<Integer> idsDeObras) {
        List<Pedido> pedidosFiltrados = new ArrayList<>();
        idsDeObras.forEach( id -> pedidosFiltrados.add(buscarPedidoPorIdObra(id).get()));

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
        String url = REST_API_URL + GET_IDS_OBRAS + finalURL;
        WebClient client = WebClient.create(url);

        return client.get()
                .uri(url).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Integer.class)
                .collectList()
                .block();
    }

}