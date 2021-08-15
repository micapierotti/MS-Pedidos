package com.dan.pgm.mspedidos.dao;

import com.dan.pgm.mspedidos.domain.DetallePedido;
import com.dan.pgm.mspedidos.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PedidoRepositoryH2 extends JpaRepository<Pedido, Integer> {

    Optional<Pedido> findByObraId(Integer id);
}
