package org.javierGomez18.fasttrack.delivery.microservicio.application.create.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.lineapedido.LineaPedidoUnprocessableEntityException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoUnprocessableEntity;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.productomenu.ProductoMenuNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteUnprocessableEntityException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.EstadoPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.LineaPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.lineaspedido.LineasPedidoRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.pedido.PedidoRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.productomenu.ProductoMenuRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.restaurante.RestauranteRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreatePedidoServiceTest {

  @Mock PedidoRepositoryPort pedidoRepositoryPort;
  @Mock LineasPedidoRepositoryPort lineasPedidoRepositoryPort;
  @Mock RestauranteRepositoryPort restauranteRepositoryPort;
  @Mock ProductoMenuRepositoryPort productoMenuRepositoryPort;
  @Mock TransactionalOperator transactionalOperator;
  @InjectMocks CreatePedidoService service;

  @BeforeEach
  void setUp() {
    // el operador real envuelve el Mono en una transacción; aquí lo dejamos pasar tal cual
    when(transactionalOperator.transactional(any(Mono.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(pedidoRepositoryPort.createPedido(any(Pedido.class)))
        .thenAnswer(
            invocation -> {
              Pedido p = invocation.getArgument(0);
              return Mono.just(
                  Pedido.builder()
                      .id(100L)
                      .restauranteId(p.getRestauranteId())
                      .clienteId(p.getClienteId())
                      .estado(p.getEstado())
                      .fechaCreacion(p.getFechaCreacion())
                      .total(p.getTotal())
                      .tiempoEstimadoMinutos(p.getTiempoEstimadoMinutos())
                      .build());
            });
    when(lineasPedidoRepositoryPort.createLineaPedidoLst(any()))
        .thenAnswer(invocation -> Flux.fromIterable(invocation.getArgument(0)));
  }

  // ------------------------------------------------------------------ helpers

  private Pedido pedidoConLineas(LineaPedido... lineas) {
    return Pedido.builder()
        .restauranteId(1L)
        .clienteId(7L)
        .estado(EstadoPedido.PEDIDO_RECIBIDO)
        .lineas(new ArrayList<>(List.of(lineas)))
        .build();
  }

  private LineaPedido linea(Long productoId, int cantidad) {
    return LineaPedido.builder().productoId(productoId).cantidad(cantidad).build();
  }

  private void restaurante(boolean activo) {
    when(restauranteRepositoryPort.getRestauranteById(1L))
        .thenReturn(Mono.just(new Restaurante(1L, "Sushi Zen", null, activo, null, null)));
  }

  private ProductoMenu producto(Long id, Long restauranteId, String precio, boolean disponible) {
    return new ProductoMenu(id, restauranteId, "Producto " + id, new BigDecimal(precio), disponible);
  }

  private void menu(ProductoMenu... productos) {
    when(productoMenuRepositoryPort.getByIds(any())).thenReturn(Flux.just(productos));
  }

  // -------------------------------------------------------------- camino feliz

  @Test
  void calculaPrecioYTotalYGuardaLineasConElIdDelPedido() {
    restaurante(true);
    menu(producto(5L, 1L, "4.50", true));

    StepVerifier.create(service.createPedido(pedidoConLineas(linea(5L, 3))))
        .assertNext(
            creado -> {
              assertThat(creado.getId()).isEqualTo(100L);
              assertThat(creado.getTotal()).isEqualByComparingTo("13.50");
              LineaPedido l = creado.getLineas().get(0);
              assertThat(l.getPedidoId()).isEqualTo(100L);
              assertThat(l.getPrecioUnitario()).isEqualByComparingTo("4.50");
            })
        .verifyComplete();
  }

  @Test
  void sumaElTotalDeVariasLineasConProductosDistintos() {
    restaurante(true);
    menu(producto(5L, 1L, "9.50", true), producto(6L, 1L, "3.50", true));

    // 2 x 9.50 + 1 x 3.50 = 22.50
    StepVerifier.create(service.createPedido(pedidoConLineas(linea(5L, 2), linea(6L, 1))))
        .assertNext(
            creado -> {
              assertThat(creado.getTotal()).isEqualByComparingTo("22.50");
              assertThat(creado.getLineas()).hasSize(2);
            })
        .verifyComplete();
  }

  @Test
  void pideUnaSolaVezCadaProductoAunqueApanezcaEnVariasLineas() {
    restaurante(true);
    menu(producto(5L, 1L, "4.00", true));

    StepVerifier.create(service.createPedido(pedidoConLineas(linea(5L, 1), linea(5L, 2))))
        .assertNext(creado -> assertThat(creado.getTotal()).isEqualByComparingTo("12.00"))
        .verifyComplete();

    verify(productoMenuRepositoryPort).getByIds(List.of(5L)); // distinct(): una sola id
  }

  @Test
  void rellenaFechaYTiempoEstimadoYMantieneElEstadoInicial() {
    restaurante(true);
    menu(producto(5L, 1L, "4.50", true));

    StepVerifier.create(service.createPedido(pedidoConLineas(linea(5L, 1))))
        .assertNext(
            creado -> {
              assertThat(creado.getFechaCreacion()).isNotNull();
              assertThat(creado.getTiempoEstimadoMinutos()).isPositive();
              assertThat(creado.getEstado()).isEqualTo(EstadoPedido.PEDIDO_RECIBIDO);
            })
        .verifyComplete();
  }

  // ------------------------------------------------------- validaciones (no guardan)

  @Test
  void pedidoSinLineasDa422SinTocarNingunPuerto() {
    StepVerifier.create(service.createPedido(pedidoConLineas()))
        .expectError(PedidoUnprocessableEntity.class)
        .verify();

    verifyNoInteractions(
        restauranteRepositoryPort, productoMenuRepositoryPort, pedidoRepositoryPort);
  }

  @Test
  void restauranteInexistenteDa404() {
    when(restauranteRepositoryPort.getRestauranteById(1L)).thenReturn(Mono.empty());

    StepVerifier.create(service.createPedido(pedidoConLineas(linea(5L, 1))))
        .expectError(RestauranteNotFoundException.class)
        .verify();
    verify(pedidoRepositoryPort, never()).createPedido(any());
  }

  @Test
  void restauranteInactivoDa422SinGuardarNada() {
    restaurante(false);

    StepVerifier.create(service.createPedido(pedidoConLineas(linea(5L, 1))))
        .expectError(RestauranteUnprocessableEntityException.class)
        .verify();
    verify(pedidoRepositoryPort, never()).createPedido(any());
    verifyNoInteractions(productoMenuRepositoryPort);
  }

  @Test
  void productoInexistenteDa404() {
    restaurante(true);
    when(productoMenuRepositoryPort.getByIds(any())).thenReturn(Flux.empty());

    StepVerifier.create(service.createPedido(pedidoConLineas(linea(5L, 1))))
        .expectError(ProductoMenuNotFoundException.class)
        .verify();
    verify(pedidoRepositoryPort, never()).createPedido(any());
  }

  @Test
  void productoDeOtroRestauranteDa422() {
    restaurante(true);
    menu(producto(5L, 99L, "4.50", true)); // pertenece al restaurante 99, el pedido es del 1

    StepVerifier.create(service.createPedido(pedidoConLineas(linea(5L, 1))))
        .expectErrorSatisfies(
            error ->
                assertThat(error)
                    .isInstanceOfSatisfying(
                        LineaPedidoUnprocessableEntityException.class,
                        e ->
                            assertThat(e.getTipo())
                                .isEqualTo(
                                    LineaPedidoUnprocessableEntityException.Tipo
                                        .PRODUCTO_NO_PERTENECE_A_RESTAURANTE)))
        .verify();
    verify(pedidoRepositoryPort, never()).createPedido(any());
  }

  @Test
  void productoNoDisponibleDa422SinGuardarNada() {
    restaurante(true);
    menu(producto(5L, 1L, "4.50", false));

    StepVerifier.create(service.createPedido(pedidoConLineas(linea(5L, 1))))
        .expectErrorSatisfies(
            error ->
                assertThat(error)
                    .isInstanceOfSatisfying(
                        LineaPedidoUnprocessableEntityException.class,
                        e ->
                            assertThat(e.getTipo())
                                .isEqualTo(
                                    LineaPedidoUnprocessableEntityException.Tipo
                                        .PRODUCTO_NO_DISPONIBLE)))
        .verify();
    verify(pedidoRepositoryPort, never()).createPedido(any());
  }

  @Test
  void siUnaLineaEsInvalidaNoSeGuardaNiSiquieraLasValidas() {
    restaurante(true);
    menu(producto(5L, 1L, "4.50", true), producto(6L, 1L, "3.00", false)); // la 2ª no disponible

    StepVerifier.create(service.createPedido(pedidoConLineas(linea(5L, 1), linea(6L, 1))))
        .expectError(LineaPedidoUnprocessableEntityException.class)
        .verify();
    verify(pedidoRepositoryPort, never()).createPedido(any());
    verify(lineasPedidoRepositoryPort, never()).createLineaPedidoLst(any());
  }

  // ---------------------------------------------------------------- transacción

  @Test
  void laTransaccionEnvuelveSoloLasEscriturasNoLasValidaciones() {
    restaurante(false); // falla en la validación, antes de llegar a guardar

    StepVerifier.create(service.createPedido(pedidoConLineas(linea(5L, 1))))
        .expectError(RestauranteUnprocessableEntityException.class)
        .verify();

    // si la validación fallara dentro de la transacción, se habría abierto una para nada
    verify(transactionalOperator, never()).transactional(any(Mono.class));
  }
}
