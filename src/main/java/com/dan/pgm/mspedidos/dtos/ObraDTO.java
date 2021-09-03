package com.dan.pgm.mspedidos.dtos;

public class ObraDTO {
	private Integer id;
	private String descripcion;
	private Float latitud;
	private Float longitud;
	private String direccion;
	private Integer superficie;
	private String tipo;
	private Integer clienteId;

	public ObraDTO(String descripcion, Float latitud, Float longitud, String direccion, Integer superficie, String tipo, Integer clienteId) {
		this.descripcion = descripcion;
		this.latitud = latitud;
		this.longitud = longitud;
		this.direccion = direccion;
		this.superficie = superficie;
		this.tipo = tipo;
		this.clienteId = clienteId;
	}
	public ObraDTO () {

	}
	public String getDescripcion() {
		return descripcion;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public Float getLatitud() {
		return latitud;
	}
	public void setLatitud(Float latitud) {
		this.latitud = latitud;
	}
	public Float getLongitud() {
		return longitud;
	}
	public void setLongitud(Float longitud) {
		this.longitud = longitud;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	public Integer getSuperficie() {
		return superficie;
	}
	public void setSuperficie(Integer superficie) {
		this.superficie = superficie;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public Integer getClienteId() {
		return clienteId;
	}
	public void setClienteId(Integer clienteId) {
		this.clienteId = clienteId;
	}
}
