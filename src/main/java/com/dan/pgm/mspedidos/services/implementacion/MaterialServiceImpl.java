package com.dan.pgm.mspedidos.services.implementacion;

import com.dan.pgm.mspedidos.dtos.MaterialDTO;
import com.dan.pgm.mspedidos.dtos.ProductoDTO;
import com.dan.pgm.mspedidos.services.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class MaterialServiceImpl implements MaterialService {

    private static final String REST_API_MATERIAL_URL = "http://localhost:9001/api/productos/";

    @Autowired
    private CircuitBreakerFactory circuitBreakerFactory;

    @Override
    public Integer stockDisponible(Integer idProducto) {
        String url = REST_API_MATERIAL_URL + idProducto;
        WebClient client = WebClient.create(url);
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");

        return circuitBreaker.run(() -> {
            ProductoDTO result = client.get()
                    .uri(url).accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(ProductoDTO.class)
                    .block();

            if(result != null)
                return result.getStockActual()-result.getStockMinimo();
            else
                throw new RuntimeException("No se pudo obtener el stock disponible: no se encontró el producto de id "+idProducto);
        }, throwable -> defaultStockDisponible());

    }

    private Integer defaultStockDisponible() {
        return 10000;
    }

    @Override
    public boolean existeMaterial(Integer idProducto) {
        String url = REST_API_MATERIAL_URL + idProducto;
        WebClient client = WebClient.create(url);
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");

        return circuitBreaker.run(() -> {
            try{
                MaterialDTO materialResult= client.get()
                        .uri(url).accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(MaterialDTO.class)
                        .block();
                return true;
            } catch (Exception e){
                return false;
            }
        }, throwable -> defaultExisteMaterial());
    }

    private boolean defaultExisteMaterial() {
        return false;
    }
}