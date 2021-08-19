package com.dan.pgm.mspedidos.services;

import com.dan.pgm.mspedidos.domain.DetallePedido;
import com.dan.pgm.mspedidos.domain.Pedido;
import com.dan.pgm.mspedidos.dtos.PedidoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface PedidoService {

    public Pedido crearPedido(Pedido p);
    public Pedido agregarDetallePedido(Integer idPedido, DetallePedido detallePedido);
    public Pedido actualizarPedido(Pedido pedido, Integer idPedido);
    public String actualizarEstado(Integer idPedido,String estado);
    public boolean borrarPedido(Integer id);
    public boolean borrarDetalleDePedido(Integer id, Integer idDetalle);
    public Pedido actualizarDetallePedido(List<DetallePedido> detalles, Integer idPedido);
    public Pedido buscarPedidoPorId(Integer id);
    public List<Pedido> buscarPedidoPorIdObra(Integer id);
    public List<Pedido> buscarPedidoPorEstado(String estado);
    public List<Pedido> pedidoPorIdClienteCuit(Integer idCliente, String cuit);
    public DetallePedido buscarDetallePorId(Integer idPedido, Integer idDetalle);
    public boolean verificarExistenciaDePedidos(ArrayList<Integer> idsDeObras);
    public List<PedidoDTO> facturasPorClienteId(Integer idCliente);

}