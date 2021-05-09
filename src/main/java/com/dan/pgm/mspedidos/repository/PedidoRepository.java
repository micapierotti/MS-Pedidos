package com.dan.pgm.mspedidos.repository;

import com.dan.pgm.mspedidos.domain.Pedido;
import frsf.isi.dan.InMemoryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoRepository  extends InMemoryRepository<Pedido> {

    @Override
    public Integer getId(Pedido arg0) {
        return arg0.getId();
    }

    @Override
    public void setId(Pedido arg0, Integer arg1) {
        arg0.setId(arg1);
    }

}