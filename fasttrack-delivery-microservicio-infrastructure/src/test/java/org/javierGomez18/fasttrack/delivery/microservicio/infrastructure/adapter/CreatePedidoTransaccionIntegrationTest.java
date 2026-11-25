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
import org.springframework.dao.DataAccessException;
import reactor.test.StepVerifier;

/**
 * Prueba lo que los tests unitarios no pueden: que el TransactionalOperator hace rollback de verdad.
 * createPedido hace DOS escrituras (fila del pedido y sus líneas); si la segunda falla, la primera
 * debe deshacerse.
 */
class CreatePedidoTransaccionIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final AtomicLong CLIENTES = new AtomicLong(30_000);

  @Autowired CreatePedidoUseCase createPedidoUseCase;
  @Autowired PedidoRepositoryPort pedidoRepositoryPort;

  private Pedido pedido(Long clienteId, int cantidad) {
    Long burgerLab = restauranteId("Burger Lab");
    return Pedido.builder()
        .restauranteId(burgerLab)
        .clienteId(clienteId)
        .estado(EstadoPedido.PEDIDO_RECIBIDO)
        .lineas(
            new ArrayList<>(
                List.of(
                    LineaPedido.builder()
                        .productoId(productoId(burgerLab, "Classic Burger"))
                        .cantidad(cantidad)
                        .build())))
        .build();
  }

  @Test
  void caminoFelizGuardaElPedidoYSusLineasJuntos() {
    Long cliente = CLIENTES.incrementAndGet();

    StepVerifier.create(createPedidoUseCase.createPedido(pedido(cliente, 2)))
        .assertNext(creado -> assertThat(creado.getId()).isNotNull())
        .verifyComplete();

    StepVerifier.create(pedidoRepositoryPort.getPedidosByClienteId(cliente).collectList())
        .assertNext(
            pedidos -> {
              assertThat(pedidos).hasSize(1);
              assertThat(pedidos.get(0).getLineas()).hasSize(1);
              assertThat(pedidos.get(0).getTotal()).isEqualByComparingTo("19.00"); // 2 x 9.50
            })
        .verifyComplete();
  }

  @Test
  void siFallaLaInsercionDeLasLineasSeDeshaceTambienElPedido() {
    Long cliente = CLIENTES.incrementAndGet();

    // cantidad 0 atraviesa las validaciones del service (la API ya lo rechazaría con 400) y
    // choca con el CHECK (cantidad >= 1) de linea_pedido, es decir, falla en la SEGUNDA escritura,
    // cuando la fila del pedido ya se ha insertado dentro de la transacción.
    StepVerifier.create(createPedidoUseCase.createPedido(pedido(cliente, 0)))
        .expectErrorMatches(e -> e instanceof DataAccessException)
        .verify();

    // Sin transacción aquí quedaría un pedido huérfano sin líneas. Con ella, no queda nada.
    StepVerifier.create(pedidoRepositoryPort.getPedidosByClienteId(cliente)).verifyComplete();
  }
}
