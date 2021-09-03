package com.dan.pgm.mspedidos.domain;
import javax.persistence.*;

@Entity
public class DetallePedido {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Integer idProducto;
	private Integer cantidad;
	private Double precio;

	public DetallePedido() {
	}
	public DetallePedido(Integer idProducto, Integer cantidad, Double precio) {
		this.idProducto = idProducto;
		this.cantidad = cantidad;
		this.precio = precio;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getIdProducto() {
		return idProducto;
	}
	public void setIdProducto(Integer idProducto) {
		this.idProducto = idProducto;
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
