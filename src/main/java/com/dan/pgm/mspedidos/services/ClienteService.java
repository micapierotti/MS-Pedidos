package com.dan.pgm.mspedidos.services;

import com.dan.pgm.mspedidos.domain.Obra;

public interface ClienteService {
    public Double deudaCliente(Obra id);
    public Double maximoSaldoNegativo(Obra id);
    public Integer situacionCrediticiaBCRA(Obra id);

}
