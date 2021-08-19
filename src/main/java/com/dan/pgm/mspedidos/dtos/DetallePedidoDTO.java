package com.dan.pgm.mspedidos.dtos;

public class DetallePedidoDTO {

    private Integer productoId;
    private Integer cantidad;
    private Double precio;

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public DetallePedidoDTO(Integer productoId, Integer cantidad, Double precio) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precio = precio;
    }
    public DetallePedidoDTO() {}
}
