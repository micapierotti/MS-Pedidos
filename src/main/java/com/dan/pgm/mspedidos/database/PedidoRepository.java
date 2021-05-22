package com.dan.pgm.mspedidos.database;

import com.dan.pgm.mspedidos.domain.Pedido;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PedidoRepository extends CrudRepository<Pedido, Integer>{

}
