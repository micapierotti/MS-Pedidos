package com.dan.pgm.mspedidos.services.implementacion;

import com.dan.pgm.mspedidos.domain.Producto;
import com.dan.pgm.mspedidos.dtos.ProductoDTO;
import com.dan.pgm.mspedidos.services.MaterialService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class MaterialServiceImpl implements MaterialService {
    private static final String REST_API_MATERIAL_URL = "http://localhost:9001/api/productos/";

    @Override
    public Integer stockDisponible(Producto p) {
        String url = REST_API_MATERIAL_URL + p.getId();
        WebClient client = WebClient.create(url);

        ProductoDTO result = client.get()
                .uri(url).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ProductoDTO.class)
                .block();

        if(result != null){
            return result.getStockActual()-result.getStockMinimo();
        }else{
            throw new RuntimeException("No se pudo obtener el stock disponible: no se encontró el producto de id "+p.getId());
        }
    }
}