package com.dan.pgm.mspedidos.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.dan.pgm.mspedidos.domain.DetallePedido;
import com.dan.pgm.mspedidos.domain.Obra;
import com.dan.pgm.mspedidos.domain.Pedido;
import com.dan.pgm.mspedidos.dtos.ClienteDTO;
import com.dan.pgm.mspedidos.dtos.ObraDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import reactor.core.publisher.Mono;

//import dan.tp2021.pedidos.service.PedidoService;

@RestController
@RequestMapping("/api/pedido")
@Api(value = "PedidoResource", description = "Permite gestionar los pedidos")
public class PedidoResource {
	
	/*
	@Autowired
	PedidoService pedidoSrv;

	public ResponseEntity<String> crearPedido(@RequestBody Pedido unPedido){
		if(unPedido.getObra()==null) {
			return ResponseEntity.badRequest().body("Debe elegir una obra");
		}
		if(unPedido.getDetalle()==null || unPedido.getDetalle().isEmpty() ) {
			return ResponseEntity.badRequest().body("Debe agregar items al pedido");
		}
		pedidoSrv.crearPedido(unPedido);
		return ResponseEntity.status(HttpStatus.CREATED).body("OK");
	}
	*/
	private static final List<Pedido> listaPedidos = new ArrayList<>();
	
	private static final String GET_OBRA = "/api/obra/{id}";
	private static final String REST_API_URL = "http://localhost:8080";
    private static Integer ID_GEN = 1;
	
    @PostMapping
    @ApiOperation(value = "Carga un pedido")
    public ResponseEntity<Pedido> crear(@RequestBody Pedido nuevo){
    	System.out.println(" crear pedido "+ nuevo);
        nuevo.setId(ID_GEN++);
        listaPedidos.add(nuevo);
        return ResponseEntity.ok(nuevo);
    }
    
    @PostMapping(path = "/{idPedido}/detalle")
    @ApiOperation(value = "Carga un detalle de pedido")
    public ResponseEntity<Pedido> agregarItem(@PathVariable Integer idPedido, @RequestBody DetallePedido nuevo){
    	System.out.println(" agregar item a pedido "+ nuevo);
    	Optional<Pedido> pedidoPorActualizar = listaPedidos
    													.stream()
    													.filter(pedido -> pedido.getId().equals(idPedido))
    													.findFirst();
        System.out.println("Lista antes de modificación: "+listaPedidos);
    	Pedido pedido = pedidoPorActualizar.get();
        
        Integer ultimoId = pedido.getDetalle().get(pedido.getDetalle().size()-1).getId();
        nuevo.setId(ultimoId+1);
        
        pedido.getDetalle().add(nuevo);
        System.out.println("Lista después de modificación: "+listaPedidos);
        
        return ResponseEntity.ok(pedido);
    }
    
    @PutMapping(path = "/{idPedido}")
    @ApiOperation(value = "Actualiza un pedido")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Actualizado correctamente"),
        @ApiResponse(code = 401, message = "No autorizado"),
        @ApiResponse(code = 403, message = "Prohibido"),
        @ApiResponse(code = 404, message = "El ID no existe")
    })
    public ResponseEntity<Pedido> actualizar(@RequestBody Pedido nuevo,  @PathVariable Integer idPedido){
        OptionalInt indexOpt =   IntStream.range(0, listaPedidos.size())
        .filter(i -> listaPedidos.get(i).getId().equals(idPedido))
        .findFirst();

        if(indexOpt.isPresent()){
            listaPedidos.set(indexOpt.getAsInt(), nuevo);
            return ResponseEntity.ok(nuevo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping(path = "/{id}")
    @ApiOperation(value = "Borra un pedido por id")
    public ResponseEntity<Pedido> borrar(@PathVariable Integer id){
        OptionalInt indexOpt =   IntStream.range(0, listaPedidos.size())
        .filter(i -> listaPedidos.get(i).getId().equals(id))
        .findFirst();

        if(indexOpt.isPresent()){
            listaPedidos.remove(indexOpt.getAsInt());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping(path = "/{id}/detalle/{idDetalle}")
    @ApiOperation(value = "Borra un detalle de pedido por id")
    public ResponseEntity<Pedido> borrarDetalle(@PathVariable Integer id, @PathVariable Integer idDetalle){
        OptionalInt indexOpt =   IntStream.range(0, listaPedidos.size())
        .filter(i -> listaPedidos.get(i).getId().equals(id))
        .findFirst();

        if(indexOpt.isPresent()){
        	OptionalInt indexOptDetalle =   IntStream.range(0, listaPedidos.get(indexOpt.getAsInt()).getDetalle().size())
        	        .filter(i -> listaPedidos.get(indexOpt.getAsInt()).getDetalle().get(i).getId().equals(idDetalle))
        	        .findFirst();
        	if(indexOptDetalle.isPresent()) {
        		listaPedidos.get(indexOpt.getAsInt()).getDetalle().remove(indexOptDetalle.getAsInt());
        		return ResponseEntity.ok().build();
        	} else {
        		return ResponseEntity.notFound().build();
        	}
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping(path = "/{id}")
    @ApiOperation(value = "Busca un pedido por id")
    public ResponseEntity<Pedido> getPedidoById(@PathVariable Integer id){
    	 Optional<Pedido> c =  listaPedidos
                 .stream()
                 .filter(unP -> unP.getId().equals(id))
                 .findFirst();
         return ResponseEntity.of(c);
    }
    
    @GetMapping(path = "/obra/{id}")
    @ApiOperation(value = "Busca un pedido por id de obra")
    public ResponseEntity<Pedido> getPedidoByIdDeObra(@PathVariable Integer id){
    	 Optional<Pedido> c =  listaPedidos
                 .stream()
                 .filter(unP -> unP.getObra().getId().equals(id))
                 .findFirst();
         return ResponseEntity.of(c);
    }
    
    @GetMapping
    @ApiOperation(value = "Busca un pedido por id de cliente y/o cuit")
    public ResponseEntity<List<Pedido>> obraPorNombre(@RequestParam(name="idCliente", required = false) Integer idCliente, @RequestParam(name="cuit", required = false) String cuit){
    	
    	List<Pedido> listaPedidosFinal = new ArrayList<>(listaPedidos);
    	System.out.println("idCliente: " + idCliente);
    	System.out.println("cuit: " + cuit);
    	
    	if(idCliente != null) {
    		for(Pedido pedido:listaPedidos) {
    			
    			Integer obraPedido = pedido.getObra().getId();
    			ObraDTO obraObtenida = getObraPorId(obraPedido);
    			
    			Integer idClienteDTO = obraObtenida.getCliente().getId();
    			
    			if(idClienteDTO != idCliente) {
    				listaPedidosFinal.remove(pedido);
    			}
    		}
    	}
    	if(cuit != null) {
    		for(Pedido pedido:listaPedidos) {
    			
    			Integer obraPedido = pedido.getObra().getId();
    			ObraDTO obraObtenida = getObraPorId(obraPedido);
    			
    			String cuitClienteDTO = obraObtenida.getCliente().getCuit();
    			
    			if(cuitClienteDTO.compareTo(cuit) != 0) {
    				listaPedidosFinal.remove(pedido);
    			}
    		}
    	}
    	
    	System.out.println("Lista a devolver: " + listaPedidosFinal);
       
        return ResponseEntity.ok(listaPedidosFinal);
    }
    
    @GetMapping(path = "/{id}/detalle/{idDetalle}")
    @ApiOperation(value = "Busca un detalle de pedido por id")
    public ResponseEntity<DetallePedido> getDetalleDePedidoById(@PathVariable Integer id, @PathVariable Integer idDetalle){
    	OptionalInt indexOpt =   IntStream.range(0, listaPedidos.size())
    	        .filter(i -> listaPedidos.get(i).getId().equals(id))
    	        .findFirst();
    	
    	if(indexOpt.isPresent()) {
    		Optional<DetallePedido> detallePedido = listaPedidos.get(indexOpt.getAsInt()).getDetalle()
       			 .stream()
       			 .filter(detalle -> detalle.getId().equals(idDetalle))
       			 .findFirst();
    		return ResponseEntity.of(detallePedido);
    	} else {
    		return ResponseEntity.notFound().build();
    	}
    }
    
    public ObraDTO getObraPorId(Integer obraPedido) {
    	
    	String url = REST_API_URL + GET_OBRA;
		WebClient client = WebClient.create(url);
		
		return client.get()
		.uri(url, obraPedido).accept(MediaType.APPLICATION_JSON)
		.retrieve()
		.bodyToMono(ObraDTO.class)
		.block();
    }
	
}
