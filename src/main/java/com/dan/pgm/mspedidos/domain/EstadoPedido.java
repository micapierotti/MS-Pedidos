package com.dan.pgm.mspedidos.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public enum EstadoPedido {
	NUEVO,
	CONFIRMADO,
	PENDIENTE,
	CANCELADO,
	ACEPTADO,
	RECHAZADO,
	EN_PREPARACION,
	ENTREGADO
}
