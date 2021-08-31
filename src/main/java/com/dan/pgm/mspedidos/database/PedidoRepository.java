package com.dan.pgm.mspedidos.database;

import com.dan.pgm.mspedidos.domain.EstadoPedido;
import com.dan.pgm.mspedidos.domain.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface PedidoRepository extends CrudRepository<Pedido, Integer>{

    Optional<List<Pedido>> findByObraId(Integer idObra);

    //@Query(value="select p from Pedido p where p.estado = ?1", nativeQuery = true)
    List<Pedido> findByEstado(EstadoPedido estado);

}
