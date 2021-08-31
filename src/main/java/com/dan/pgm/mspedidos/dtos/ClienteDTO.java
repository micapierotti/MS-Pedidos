package com.dan.pgm.mspedidos.dtos;

import java.util.Date;
import java.util.List;

public class ClienteDTO {
	 private Integer id;
	    private String razonSocial;
	    private String cuit;
	    private String mail;
	    private Double maxCuentaCorriente;
	    private Boolean habilitadoOnline;
	    private UserDTO user;
	    private List<ObraDTO> obras;
		private Date fechaBaja;
	    public ClienteDTO() {
	    	
	    }
	    
		public ClienteDTO(Integer id, String razonSocial, String cuit, String mail, Double maxCuentaCorriente,
				Boolean habilitadoOnline, UserDTO user, List<ObraDTO> obras) {
			super();
			this.id = id;
			this.razonSocial = razonSocial;
			this.cuit = cuit;
			this.mail = mail;
			this.maxCuentaCorriente = maxCuentaCorriente;
			this.habilitadoOnline = habilitadoOnline;
			this.user = user;
			this.obras = obras;
		}
		
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
		public Boolean getHabilitadoOnline() {
			return habilitadoOnline;
		}
		public void setHabilitadoOnline(Boolean habilitadoOnline) {
			this.habilitadoOnline = habilitadoOnline;
		}
		public UserDTO getUser() {
			return user;
		}
		public void setUser(UserDTO user) {
			this.user = user;
		}
		public List<ObraDTO> getObras() {
			return obras;
		}
		public void setObras(List<ObraDTO> obras) {
			this.obras = obras;
		}
	    
	    
}
