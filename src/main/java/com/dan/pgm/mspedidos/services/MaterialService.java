package com.dan.pgm.mspedidos.services;

public interface MaterialService {
    Integer stockDisponible(Integer idProducto);
    boolean existeMaterial(Integer idProducto);
}
