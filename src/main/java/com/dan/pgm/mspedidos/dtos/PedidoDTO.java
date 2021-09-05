package com.dan.pgm.mspedidos.dtos;

import java.util.List;

public class PedidoDTO {
    private Integer id;
    private List<DetallePedidoDTO> detalle;

    public PedidoDTO(Integer id, List<DetallePedidoDTO> detalle) {
        this.id = id;
        this.detalle = detalle;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public List<DetallePedidoDTO> getDetalle() {
        return detalle;
    }
    public void setDetalle(List<DetallePedidoDTO> detalle) {
        this.detalle = detalle;
    }
}
