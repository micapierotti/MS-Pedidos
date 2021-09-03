package com.dan.pgm.mspedidos.dtos;

import java.util.List;

public class ClienteDTO {
	private Integer id;
	private String razonSocial;
	private String cuit;
	private String mail;
	private Double maxCuentaCorriente;
	private String username;
	private String password;
	private List<ObraDTO> obras;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getRazonSocial() {
		return razonSocial;
	}
	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}
	public String getCuit() {
		return cuit;
	}
	public void setCuit(String cuit) {
		this.cuit = cuit;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public Double getMaxCuentaCorriente() {
		return maxCuentaCorriente;
	}
	public void setMaxCuentaCorriente(Double maxCuentaCorriente) {
		this.maxCuentaCorriente = maxCuentaCorriente;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<ObraDTO> getObras() {
		return obras;
	}
	public void setObras(List<ObraDTO> obras) {
		this.obras = obras;
	}
}
