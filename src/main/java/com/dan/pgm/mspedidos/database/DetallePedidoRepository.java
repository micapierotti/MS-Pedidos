package com.dan.pgm.mspedidos.database;

import com.dan.pgm.mspedidos.domain.DetallePedido;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallePedidoRepository extends CrudRepository<DetallePedido, Integer>{
}
