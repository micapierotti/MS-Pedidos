package com.dan.pgm.mspedidos.dtos;

public class DetallePedidoDTO {
    private Integer id;
    private Integer productoId;
    private Integer cantidad;
    private Double precio;

    public DetallePedidoDTO(Integer id, Integer productoId, Integer cantidad, Double precio) {
        this.id = id;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public DetallePedidoDTO() {
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
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
}
