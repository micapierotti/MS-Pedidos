package com.dan.pgm.mspedidos.database;

import com.dan.pgm.mspedidos.domain.EstadoPedido;
import com.dan.pgm.mspedidos.domain.Pedido;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends CrudRepository<Pedido, Integer>{

    Optional<List<Pedido>> findByIdObra(Integer idObra);
    List<Pedido> findByEstado(EstadoPedido estado);
}
