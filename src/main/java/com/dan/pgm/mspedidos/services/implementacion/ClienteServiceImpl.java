package com.dan.pgm.mspedidos.services.implementacion;

import com.dan.pgm.mspedidos.domain.Obra;
import com.dan.pgm.mspedidos.dtos.ClienteDTO;
import com.dan.pgm.mspedidos.dtos.ObraDTO;
import com.dan.pgm.mspedidos.services.ClienteService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ClienteServiceImpl implements ClienteService {
    private static final String REST_API_OBRA_URL = "http://localhost:9000/api/obra/";
    private static final String REST_API_CLIENTE_URL = "http://localhost:9000/api/cliente/";
    private static final String GET_CLIENTE_BY_ID = "by-id/";

    @Override
    public Double deudaCliente(Integer id) {
        String obraUrl = REST_API_OBRA_URL + id;
        WebClient obraClient = WebClient.create(obraUrl);

        ObraDTO obraResult = obraClient.get()
                .uri(obraUrl).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ObraDTO.class)
                .block();

        if(obraResult != null){
            String clientUrl = REST_API_CLIENTE_URL +  GET_CLIENTE_BY_ID + obraResult.getClienteId();
            WebClient client = WebClient.create(clientUrl);

            ClienteDTO clientResult = client.get()
                    .uri(clientUrl).accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(ClienteDTO.class)
                    .block();

            if(clientResult!=null){
                return clientResult.getMaxCuentaCorriente();
            } else{
                throw new RuntimeException("No se encontró el cliente de la obra "+id);
            }
        }else{
            throw new RuntimeException("No se encontró la obra de id "+id);
        }
    }

    @Override
    public Integer situacionCrediticiaBCRA(Obra id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double maximoSaldoNegativo(Obra id) {
        // TODO Auto-generated method stub
        return null;
    }

}