package com.dan.pgm.mspedidos.services;

import com.dan.pgm.mspedidos.domain.DetallePedido;
import com.dan.pgm.mspedidos.domain.Pedido;
import com.dan.pgm.mspedidos.dtos.PedidoDTO;

import java.util.ArrayList;
import java.util.List;

public interface PedidoService {

    Pedido crearPedido(Pedido p);
    Pedido agregarDetallePedido(Integer idPedido, DetallePedido detallePedido);
    Pedido actualizarPedido(Pedido pedido, Integer idPedido);
    String actualizarEstado(Integer idPedido,String estado);
    boolean borrarPedido(Integer id);
    boolean borrarDetalleDePedido(Integer id, Integer idDetalle);
    Pedido actualizarDetallePedido(List<DetallePedido> detalles, Integer idPedido);
    Pedido buscarPedidoPorId(Integer id);
    List<Pedido> buscarPedidoPorIdObra(Integer id);
    List<Pedido> buscarPedidoPorEstado(String estado);
    List<Pedido> pedidoPorIdCliente(Integer idCliente);
    DetallePedido buscarDetallePorId(Integer idPedido, Integer idDetalle);
    boolean verificarExistenciaDePedidos(ArrayList<Integer> idsDeObras);
    boolean existeObra(Integer idObra);
    DetallePedido buscarDetallePorSuId(Integer idDetalle);
    public List<PedidoDTO> facturasPorClienteId(Integer idCliente);
}