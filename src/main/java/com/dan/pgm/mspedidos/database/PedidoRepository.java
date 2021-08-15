package com.dan.pgm.mspedidos.database;

import com.dan.pgm.mspedidos.domain.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends CrudRepository<Pedido, Integer>{

    Optional<List<Pedido>> findByObraId(Integer idObra);

    @Query("select * from Pedido p where p.estado = ?1")
    List<Pedido> findByEstado(String estado);

}
