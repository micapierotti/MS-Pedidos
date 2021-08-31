package com.dan.pgm.mspedidos.services;

import com.dan.pgm.mspedidos.domain.Producto;

public interface MaterialService {
    Integer stockDisponible(Producto m);
}
