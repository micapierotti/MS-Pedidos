package com.dan.pgm.mspedidos.services;

import com.dan.pgm.mspedidos.domain.DetallePedido;
import com.dan.pgm.mspedidos.domain.Pedido;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface PedidoService {

    public Pedido crearPedido(Pedido p);
    public Pedido agregarDetallePedido(Integer idPedido, DetallePedido detallePedido);
    public Pedido actualizarPedido(Pedido pedido, Integer idPedido);
    public boolean borrarPedido(Integer id);
    public boolean borrarDetalleDePedido(Integer id, Integer idDetalle);
    public Optional<Pedido> buscarPedidoPorId(Integer id);
    public Optional<Pedido> buscarPedidoPorIdObra(Integer id);
    public List<Pedido> pedidoPorIdClienteCuit(Integer idCliente, String cuit);
    public DetallePedido buscarDetallePorId(Integer idPedido, Integer idDetalle);
    public boolean verificarExistenciaDePedidos(ArrayList<Integer> idsDeObras);

}