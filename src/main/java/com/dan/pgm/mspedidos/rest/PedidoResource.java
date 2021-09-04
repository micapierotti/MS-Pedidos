package com.dan.pgm.mspedidos.rest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.dan.pgm.mspedidos.domain.EstadoPedido;
import com.dan.pgm.mspedidos.services.MaterialService;
import com.dan.pgm.mspedidos.services.PedidoService;
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
import com.dan.pgm.mspedidos.domain.Pedido;
import com.dan.pgm.mspedidos.dtos.ObraDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/pedido")
@Api(value = "PedidoResource", description = "Permite gestionar los pedidos")
public class PedidoResource {

	@Autowired
    PedidoService pedidoSrv;

    @Autowired
    MaterialService materialSrv;

    @PostMapping(path = "/new")
    @ApiOperation(value = "Carga un pedido")
    public ResponseEntity<String> crear(@RequestBody Pedido unPedido){

        System.out.println(" Crear pedido "+ unPedido);

        if(unPedido.getIdObra()==null)
            return ResponseEntity.badRequest().body("Debe elegir una obra");
        if(!pedidoSrv.existeObra(unPedido.getIdObra()))
            return ResponseEntity.badRequest().body("No existe la obra de id "+unPedido.getIdObra()+", solo se pueden crear pedidos de obras que ya existan en la base de datos.");
        if(unPedido.getDetalle()==null || unPedido.getDetalle().isEmpty() )
            return ResponseEntity.badRequest().body("Debe agregar items al pedido");
        for(DetallePedido dP : unPedido.getDetalle()) {
            if(dP.getCantidad() == null)
                return ResponseEntity.badRequest().body("Por favor, especifique una cantidad para el detalle "+dP.getId());
            if(dP.getCantidad() <= 0)
                return ResponseEntity.badRequest().body("La cantidad en el detalle "+dP.getId()+" debe ser mayor a 0");
            if(dP.getIdProducto() == null)
                return ResponseEntity.badRequest().body("El detalle "+dP.getId()+" debe especificar un producto");
            if(!materialSrv.existeMaterial(dP.getIdProducto()))
                return ResponseEntity.badRequest().body("No existe el producto de id "+dP.getIdProducto()+", solo se pueden crear pedidos con productos existentes.");
        }

        unPedido.setEstado(EstadoPedido.NUEVO);
        unPedido.setFechaPedido(Instant.now());
        pedidoSrv.crearPedido(unPedido);
        return ResponseEntity.status(HttpStatus.CREATED).body("OK");
    }
    
    @PostMapping(path = "/detalle/{idPedido}")
    @ApiOperation(value = "Carga un detalle de pedido")
    public ResponseEntity<Pedido> agregarItem(@PathVariable Integer idPedido, @RequestBody DetallePedido nuevo){
        Pedido p = pedidoSrv.agregarDetallePedido(idPedido, nuevo);

        if(p != null)
            return ResponseEntity.ok(p);
        else
            return ResponseEntity.notFound().build();
    }

    @PutMapping("/modificarEstado/{idPedido}")
    public ResponseEntity<?> actualizarEstado( @PathVariable Integer idPedido,@RequestParam String estado){
        try{
            return new ResponseEntity<String>(pedidoSrv.actualizarEstado(idPedido, estado), HttpStatus.OK);
        }catch(Exception e){
            return (ResponseEntity<?>) ResponseEntity.badRequest();
        }
    }

    @PutMapping(path = "/actualizar/{idPedido}")
    @ApiOperation(value = "Actualiza un pedido")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Actualizado correctamente"),
        @ApiResponse(code = 401, message = "No autorizado"),
        @ApiResponse(code = 403, message = "Prohibido"),
        @ApiResponse(code = 404, message = "El ID no existe")
    })
    public ResponseEntity<Pedido> actualizar(@RequestBody Pedido nuevo,  @PathVariable Integer idPedido){
        return ResponseEntity.ok(pedidoSrv.actualizarPedido(nuevo, idPedido));
    }

    @DeleteMapping(path = "/borrar/{id}")
    @ApiOperation(value = "Borra un pedido por id")
    public ResponseEntity<String> borrar(@PathVariable Integer id){
        boolean result = pedidoSrv.borrarPedido(id);

        if(result) return ResponseEntity.ok("Se ha borrado exitosamente el pedido "+id);
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping(path = "/borrarDetalle/{id}/detalle/{idDetalle}")
    @ApiOperation(value = "Borra un detalle de pedido por id")
    public ResponseEntity<Pedido> borrarDetalle(@PathVariable Integer id, @PathVariable Integer idDetalle){
        boolean result = pedidoSrv.borrarDetalleDePedido(id, idDetalle);

        if(result){
            Pedido pedido = pedidoSrv.buscarPedidoPorId(id);
            return ResponseEntity.ok(pedido);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping(path = "/actualizar-detalle/{idPedido}")
    @ApiOperation(value = "Actualiza detalle pedido")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Actualizado correctamente"),
            @ApiResponse(code = 401, message = "No autorizado"),
            @ApiResponse(code = 403, message = "Prohibido"),
            @ApiResponse(code = 404, message = "El ID no existe")
    })
    public ResponseEntity<List<DetallePedido>> actualizarDetalle(@RequestBody List<DetallePedido> nuevosDetalles,  @PathVariable Integer idPedido){
        Pedido p = pedidoSrv.actualizarDetallePedido(nuevosDetalles, idPedido);

        if(p != null)
            return ResponseEntity.ok(p.getDetalle());
        else
            return  ResponseEntity.notFound().build();
    }
    
    @GetMapping(path = "/id/{id}")
    @ApiOperation(value = "Busca un pedido por id")
    public ResponseEntity<Pedido> getPedidoById(@PathVariable Integer id){
    Pedido p = pedidoSrv.buscarPedidoPorId(id);

        if(p != null)
            return ResponseEntity.ok(p);
        else
            return ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/estado/{state}")
    @ApiOperation(value = "Busca un pedido por Estado")
    public ResponseEntity<List<Pedido>> getPedidoByEstado(@PathVariable String state){
        List<Pedido> pedidos = pedidoSrv.buscarPedidoPorEstado(state);

        if(pedidos != null)
            return ResponseEntity.ok(pedidos);
        else
            return ResponseEntity.notFound().build();
    }
    
    @GetMapping(path = "/obra/{id}")
    @ApiOperation(value = "Busca un pedido por id de obra")
    public ResponseEntity<List<Pedido>> getPedidoByIdDeObra(@PathVariable Integer id){
        List<Pedido> pedidos = pedidoSrv.buscarPedidoPorIdObra(id);

        if(pedidos.size() > 0)
            return ResponseEntity.ok(pedidos);
        else
            return ResponseEntity.notFound().build();
    }
    
    @GetMapping(path = "/buscar")
    @ApiOperation(value = "Busca un pedido por id de cliente")
    public ResponseEntity<List<Pedido>> pedidoPorIdCliente(@RequestParam Integer idCliente){
        List<Pedido> pedidos = pedidoSrv.pedidoPorIdCliente(idCliente);

        if(pedidos.size() > 0)
            return ResponseEntity.ok(pedidos);
        else
            return ResponseEntity.notFound().build();
    }
    
    @GetMapping(path = "/{id}/detalle/{idDetalle}")
    @ApiOperation(value = "Busca un detalle de pedido por id")
    public ResponseEntity<DetallePedido> getDetalleDePedidoById(@PathVariable Integer id, @PathVariable Integer idDetalle){
        DetallePedido resultado = pedidoSrv.buscarDetallePorId(id, idDetalle);

        if(resultado != null)
            return ResponseEntity.ok(resultado);
        else
            return ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/detalle/{idDetalle}")
    @ApiOperation(value = "Busca un detalle por id")
    public ResponseEntity<DetallePedido> getDetalleById(@PathVariable Integer idDetalle){
        DetallePedido resultado = pedidoSrv.buscarDetallePorSuId(idDetalle);

        if(resultado != null)
            return ResponseEntity.ok(resultado);
        else
            return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/existen-pedidos")
    @ApiOperation(value = "Devuelve si alguna de las obras recibidas tiene un pedido en curso")
    public boolean verificarExistenciaDePedidos(@RequestBody ArrayList<Integer> idsDeObras){
        return pedidoSrv.verificarExistenciaDePedidos(idsDeObras);
    }
}
