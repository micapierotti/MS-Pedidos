package com.dan.pgm.mspedidos.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
public class Pedido implements Serializable {
	@Id
	private Integer id;
	private Instant fechaPedido;
	private Integer idObra;
	@OneToMany(targetEntity = DetallePedido.class, cascade = CascadeType.ALL,
				fetch = FetchType.LAZY)
	private List<DetallePedido> detalle;
	@Enumerated(EnumType.STRING)
	private EstadoPedido estado;

	public Pedido() {
	}
	public Pedido(Integer id, Instant fechaPedido, Integer idObra, List<DetallePedido> detalle, EstadoPedido estado) {
		this.id = id;
		this.fechaPedido = fechaPedido;
		this.idObra = idObra;
		this.detalle = detalle;
		this.estado = estado;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Instant getFechaPedido() {
		return fechaPedido;
	}
	public void setFechaPedido(Instant fechaPedido) {
		this.fechaPedido = fechaPedido;
	}
	public Integer getIdObra() {
		return idObra;
	}
	public void setIdObra(Integer idObra) {
		this.idObra = idObra;
	}
	public List<DetallePedido> getDetalle() {
		return detalle;
	}
	public void setDetalle(List<DetallePedido> detalle) {
		this.detalle = detalle;
	}
	public EstadoPedido getEstado() {
		return estado;
	}
	public void setEstado(EstadoPedido estado) {
		this.estado = estado;
	}
}
