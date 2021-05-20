package com.dan.pgm.mspedidos.services.implementacion;

import com.dan.pgm.mspedidos.domain.*;
import com.dan.pgm.mspedidos.repository.PedidoRepository;
import com.dan.pgm.mspedidos.services.ClienteService;
import com.dan.pgm.mspedidos.services.MaterialService;
import com.dan.pgm.mspedidos.services.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    MaterialService materialSrv;

    @Autowired
    PedidoRepository repo;

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
        return this.repo.save(p);
    }

    @Override
    public Pedido agregarDetallePedido(Integer idPedido, DetallePedido detallePedido) {
        return null;
    }

    @Override
    public Optional<Pedido> actualizarPedido(Pedido pedido, Integer idPedido) {
        return Optional.empty();
    }

    @Override
    public boolean borrarPedido(Integer id) {
        return false;
    }

    @Override
    public boolean borrarDetalleDePedido(Integer id, Integer idDetalle) {
        return false;
    }

    @Override
    public Optional<Pedido> buscarPedidoPorId(Integer id) {
        return Optional.empty();
    }

    @Override
    public Optional<Pedido> buscarPedidoPorIdObra(Integer id) {
        return Optional.empty();
    }

    @Override
    public Optional<List<Pedido>> pedidoPorIdClienteCuit(Integer idCliente, String cuit) {
        return Optional.empty();
    }

    @Override
    public Optional<DetallePedido> buscarDetallePorId(Integer idPedido, Integer idDetalle) {
        return Optional.empty();
    }

    //TODO Terminar método una vez esté implementada la bdd
    @Override
    public boolean verificarExistenciaDePedidos(ArrayList<Integer> idsDeObras) {

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

}