package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.EstadoPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.LineaPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.CreatePedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.pedido.PedidoRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

/**
 * Los pedidos de ejemplo de data.sql (clientes 1, 2 y 3) se usan solo para leer. Los tests que
 * escriben crean su propio pedido con un cliente único.
 */
class PedidoAdapterIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final AtomicLong CLIENTES = new AtomicLong(20_000);

  @Autowired PedidoRepositoryPort port;
  @Autowired CreatePedidoUseCase createPedidoUseCase;

  private Pedido pedidoSembrado(Long clienteId, EstadoPedido estado) {
    return port.getPedidosByClienteId(clienteId)
        .filter(p -> p.getEstado() == estado)
        .blockFirst();
  }

  @Test
  void getPedidoByIdCargaElPedidoConSusLineas() {
    Long id = pedidoSembrado(1L, EstadoPedido.ENTREGADO).getId();

    StepVerifier.create(port.getPedidoById(id))
        .assertNext(
            pedido -> {
              assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.ENTREGADO);
              assertThat(pedido.getTotal()).isEqualByComparingTo("22.50");
              assertThat(pedido.getLineas()).hasSize(2); // 2 x Classic Burger + 1 x Patatas
              assertThat(pedido.getLineas()).extracting(LineaPedido::getCantidad)
                  .containsExactlyInAnyOrder(2, 1);
              assertThat(pedido.getLineas())
                  .allSatisfy(l -> assertThat(l.getPedidoId()).isEqualTo(id));
            })
        .verifyComplete();
  }

  @Test
  void getPedidoByIdInexistenteCompletaVacioSinError() {
    StepVerifier.create(port.getPedidoById(Long.MAX_VALUE)).verifyComplete();
  }

  @Test
  void getPedidosByClienteIdReparteLasLineasEnCadaPedidoSinMezclarlas() {
    // El cliente 1 tiene 2 pedidos (ENTREGADO y EN_REPARTO) con 2 líneas cada uno. Se cargan con
    // UNA sola consulta IN y se agrupan por pedido: si el agrupado fallara, se mezclarían.
    StepVerifier.create(port.getPedidosByClienteId(1L).collectList())
        .assertNext(
            pedidos -> {
              assertThat(pedidos).hasSizeGreaterThanOrEqualTo(2);
              for (Pedido pedido : pedidos) {
                assertThat(pedido.getLineas()).isNotEmpty();
                assertThat(pedido.getLineas())
                    .allSatisfy(l -> assertThat(l.getPedidoId()).isEqualTo(pedido.getId()));
              }
            })
        .verifyComplete();
  }

  @Test
  void getPedidosByClienteIdSoloDevuelvePedidosDeEseCliente() {
    StepVerifier.create(port.getPedidosByClienteId(2L).collectList())
        .assertNext(
            pedidos ->
                assertThat(pedidos).allSatisfy(p -> assertThat(p.getClienteId()).isEqualTo(2L)))
        .verifyComplete();
  }

  @Test
  void getPedidosByClienteIdSinPedidosCompletaVacioSinError() {
    StepVerifier.create(port.getPedidosByClienteId(999_999_999L)).verifyComplete();
  }

  @Test
  void getPedidosByRestauranteIdCargaLasLineas() {
    Long burgerLab = restauranteId("Burger Lab");

    StepVerifier.create(port.getPedidosByRestauranteId(burgerLab).collectList())
        .assertNext(
            pedidos -> {
              assertThat(pedidos).isNotEmpty();
              assertThat(pedidos).allSatisfy(p -> assertThat(p.getLineas()).isNotEmpty());
            })
        .verifyComplete();
  }

  @Test
  void cancelPedidoPersisteElEstadoYDevuelveElPedidoConSusLineas() {
    Pedido creado = crearPedidoDeEjemplo();

    creado.setEstado(EstadoPedido.CANCELADO);
    StepVerifier.create(port.cancelPedido(creado))
        .assertNext(
            cancelado -> {
              assertThat(cancelado.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
              assertThat(cancelado.getLineas()).hasSize(1); // las líneas no se pierden
            })
        .verifyComplete();

    // y está realmente en la BBDD, no solo en el objeto devuelto
    StepVerifier.create(port.getPedidoById(creado.getId()))
        .assertNext(
            releido -> {
              assertThat(releido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
              assertThat(releido.getLineas()).hasSize(1);
            })
        .verifyComplete();
  }

  private Pedido crearPedidoDeEjemplo() {
    Long burgerLab = restauranteId("Burger Lab");
    Pedido pedido =
        Pedido.builder()
            .restauranteId(burgerLab)
            .clienteId(CLIENTES.incrementAndGet())
            .estado(EstadoPedido.PEDIDO_RECIBIDO)
            .lineas(
                new ArrayList<>(
                    List.of(
                        LineaPedido.builder()
                            .productoId(productoId(burgerLab, "Classic Burger"))
                            .cantidad(1)
                            .build())))
            .build();
    return createPedidoUseCase.createPedido(pedido).block();
  }
}
