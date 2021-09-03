package com.dan.pgm.mspedidos.service.impl;

import com.dan.pgm.mspedidos.domain.*;
import com.dan.pgm.mspedidos.repository.PedidoRepositoryH2;
import com.dan.pgm.mspedidos.services.ClienteService;
import com.dan.pgm.mspedidos.services.MaterialService;
import com.dan.pgm.mspedidos.services.PedidoService;
import com.dan.pgm.mspedidos.services.implementacion.PedidoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class PedidoServiceImplUnitTest {

    @Autowired
    PedidoService pedidoService;

    @Autowired
    PedidoServiceImpl pedidoServiceImpl;

    @MockBean
    PedidoRepositoryH2 pedidoRepo;

    @MockBean
    ClienteService clienteService;

    @MockBean
    MaterialService materialService;

    Pedido unPedido;
    Pedido unPedido2;

    /*@BeforeEach
    void setUp() throws Exception {
        unPedido = new Pedido();
        Obra obra = new Obra();
        DetallePedido d1 = new DetallePedido(new Producto(),5,40.0);
        DetallePedido d2 = new DetallePedido(new Producto(),10,80.0);
        DetallePedido d3 = new DetallePedido(new Producto(),2,450.0);
        unPedido.setDetalle(new ArrayList<DetallePedido>());
        unPedido.getDetalle().add(d1);
        unPedido.getDetalle().add(d2);
        unPedido.getDetalle().add(d3);
        unPedido.setObra(obra);

        unPedido2 = new Pedido();
        Obra obra2 = new Obra();
        DetallePedido d11 = new DetallePedido(new Producto(),5,4000.0);
        DetallePedido d22 = new DetallePedido(new Producto(),10,8000.0);
        DetallePedido d33 = new DetallePedido(new Producto(),2,4500.0);
        unPedido2.setDetalle(new ArrayList<DetallePedido>());
        unPedido2.getDetalle().add(d11);
        unPedido2.getDetalle().add(d22);
        unPedido2.getDetalle().add(d33);
        unPedido2.setObra(obra2);
    }

    @Test
    void testCrearPedidoConStockSinDeuda() {
//		when(materialService.stockDisponible(p1)).thenReturn(29);
        when(materialService.stockDisponible(any(Producto.class))).thenReturn(20);
        // el cliente no tiene deuda
        when(clienteService.deudaCliente(any(Obra.class))).thenReturn(0.0);
        // el saldo negativo maximo es 10000
        when(clienteService.maximoSaldoNegativo(any(Obra.class))).thenReturn(10000.0);
        // el saldo negativo maximo es 10000
        when(clienteService.situacionCrediticiaBCRA(any(Obra.class))).thenReturn(1);
        // retorno el pedido
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);
//		when(clienteService.deudaCliente(argThat( (Obra o) -> o.getId()>99))).thenReturn(0.0);

        Pedido pedidoResultado = pedidoService.crearPedido(unPedido);
        assertThat(pedidoResultado.getEstado().equals(EstadoPedido.ACEPTADO));
        verify(pedidoRepo,times(1)).save(unPedido);
    }

    @Test
    void pedidoSinStockDeberiaDarEstado2() {
//		when(materialService.stockDisponible(p1)).thenReturn(29);
        when(materialService.stockDisponible(any(Producto.class))).thenReturn(3);
        // el cliente no tiene deuda
        when(clienteService.deudaCliente(any(Obra.class))).thenReturn(0.0);
        // el saldo negativo maximo es 10000
        when(clienteService.maximoSaldoNegativo(any(Obra.class))).thenReturn(10000.0);
        // el saldo negativo maximo es 10000
        when(clienteService.situacionCrediticiaBCRA(any(Obra.class))).thenReturn(1);
        // retorno el pedido
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);
//		when(clienteService.deudaCliente(argThat( (Obra o) -> o.getId()>99))).thenReturn(0.0);

        Pedido pedidoResultado = pedidoService.crearPedido(unPedido);
        assertThat(pedidoResultado.getEstado().equals(EstadoPedido.PENDIENTE));
        verify(pedidoRepo,times(1)).save(unPedido);
    }

    @Test
    void testCrearPedidoSinStockSinDeuda() {
        when(materialService.stockDisponible(any(Producto.class))).thenReturn(0);
        // el cliente no tiene deuda
        when(clienteService.deudaCliente(any(Obra.class))).thenReturn(0.0);
        // el saldo negativo maximo es 10000
        when(clienteService.maximoSaldoNegativo(any(Obra.class))).thenReturn(10000.0);
        // el saldo negativo maximo es 10000
        when(clienteService.situacionCrediticiaBCRA(any(Obra.class))).thenReturn(1);
        // retorno el pedido
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido);
//		when(clienteService.deudaCliente(argThat( (Obra o) -> o.getId()>99))).thenReturn(0.0);

        Pedido pedidoResultado = pedidoService.crearPedido(unPedido);
        assertThat(pedidoResultado.getEstado().equals(EstadoPedido.PENDIENTE));
        verify(pedidoRepo,times(1)).save(unPedido);

    }

    @Test
    void testVerificarStock() {

        when(materialService.stockDisponible(any(Producto.class))).thenReturn(20);
        int cantidad = unPedido.getDetalle().get(0).getCantidad();

        assertThat(pedidoServiceImpl.verificarStock(unPedido.getDetalle().get(0).getProducto(), cantidad));
    }

    @Test
    void testVerificarStockCuandoNoDisp() {

        when(materialService.stockDisponible(any(Producto.class))).thenReturn(0);
        int cantidad = unPedido.getDetalle().get(0).getCantidad();

        assertFalse(pedidoServiceImpl.verificarStock(unPedido.getDetalle().get(0).getProducto(), cantidad));
    }

    @Test
    void testNoCrearPedidoConStockConDeuda() {
//		when(materialService.stockDisponible(p1)).thenReturn(29);
        when(materialService.stockDisponible(any(Producto.class))).thenReturn(20);
        // el cliente no tiene deuda
        when(clienteService.deudaCliente(any(Obra.class))).thenReturn(0.0);
        // el saldo negativo maximo es 10000
        when(clienteService.maximoSaldoNegativo(any(Obra.class))).thenReturn(10000.0);
        // el saldo negativo maximo es 10000
        when(clienteService.situacionCrediticiaBCRA(any(Obra.class))).thenReturn(4);
        // retorno el pedido
        when(pedidoRepo.save(any(Pedido.class))).thenReturn(unPedido2);
//		when(clienteService.deudaCliente(argThat( (Obra o) -> o.getId()>99))).thenReturn(0.0);

        //WHEN DE ESBAJORIESGO
        //when(pedidoServiceImpl.esDeBajoRiesgo(any(Obra.class), anyDouble())).thenReturn(false);

        Throwable exception = assertThrows(
				RuntimeException.class,
			              () -> pedidoService.crearPedido(unPedido2)
			);
			assertEquals("No tiene aprobacion crediticia", exception.getMessage());
    }

    public void hacerAlgo() {
        System.out.println("hace algo");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new IndexOutOfBoundsException(-99);
    }

	@Test
	void testEsDeBajoRiesgo() {
        *//*
        * public boolean esDeBajoRiesgo(Obra o, Double saldoNuevo) {
        Double maximoSaldoNegativo = clienteSrv.maximoSaldoNegativo(o);
        Boolean tieneSaldo = Math.abs(saldoNuevo) < maximoSaldoNegativo;
        return tieneSaldo;
    }*//*
		when(clienteService.maximoSaldoNegativo(any(Obra.class))).thenReturn(10000.0);
        assertThat(pedidoServiceImpl.esDeBajoRiesgo(unPedido.getObra(), 9000.0));

	}

*//*
	@Test
	public void whenAssertingTimeout_thenNotExceeded() {
	    assertTimeout(
	      Duration.ofSeconds(2),
	      () -> hacerAlgo()
	    );
	}

	@Test
	void verificarAll() {
	  Producto p= new Producto();
	  p.setDescripcion("PRoducto1");
	  p.setPrecio(100.0);
	  assertAll("producto",
	                () -> assertEquals("PRoducto1", p.getDescripcion()),
	                () -> assertEquals(100.0, p.getPrecio())
	            );
	  }
       *//*



    @AfterEach
    void tearDown() throws Exception {
    }
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }*/

}
