package org.javierGomez18.fasttrack.delivery.microservicio.application.cancel.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.EstadoPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.LineaPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.pedido.PedidoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CancelPedidoServiceTest {

  @Mock PedidoRepositoryPort pedidoRepositoryPort;
  @InjectMocks CancelPedidoService service;

  private Pedido pedido(EstadoPedido estado) {
    return Pedido.builder()
        .id(1L)
        .estado(estado)
        .lineas(List.of(LineaPedido.builder().id(10L).productoId(5L).cantidad(1).build()))
        .build();
  }

  /** Cualquier estado anterior a ENTREGADO permite cancelar. */
  @ParameterizedTest
  @EnumSource(
      value = EstadoPedido.class,
      names = {"PEDIDO_RECIBIDO", "EN_PREPARACION", "REPARTIDOR_ASIGNADO", "EN_REPARTO"})
  void cancelaLosPedidosQueTodaviaSePuedenCancelar(EstadoPedido estado) {
    when(pedidoRepositoryPort.getPedidoById(1L)).thenReturn(Mono.just(pedido(estado)));
    when(pedidoRepositoryPort.cancelPedido(any()))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

    StepVerifier.create(service.cancelPedido(1L))
        .assertNext(cancelado -> assertThat(cancelado.getEstado()).isEqualTo(EstadoPedido.CANCELADO))
        .verifyComplete();
  }

  @ParameterizedTest
  @EnumSource(
      value = EstadoPedido.class,
      names = {"ENTREGADO", "CANCELADO"})
  void noCancelaPedidosYaEntregadosONiYaCancelados(EstadoPedido estado) {
    when(pedidoRepositoryPort.getPedidoById(1L)).thenReturn(Mono.just(pedido(estado)));

    StepVerifier.create(service.cancelPedido(1L))
        .expectError(PedidoConflictException.class)
        .verify();
    verify(pedidoRepositoryPort, never()).cancelPedido(any()); // y no escribe nada
  }

  @Test
  void pedidoInexistenteLanzaNotFoundYNoEscribe() {
    when(pedidoRepositoryPort.getPedidoById(99L)).thenReturn(Mono.empty());

    StepVerifier.create(service.cancelPedido(99L))
        .expectError(PedidoNotFoundException.class)
        .verify();
    verify(pedidoRepositoryPort, never()).cancelPedido(any());
  }

  @Test
  void conservaLasLineasDelPedidoAlCancelar() {
    when(pedidoRepositoryPort.getPedidoById(1L))
        .thenReturn(Mono.just(pedido(EstadoPedido.PEDIDO_RECIBIDO)));
    when(pedidoRepositoryPort.cancelPedido(any()))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

    StepVerifier.create(service.cancelPedido(1L))
        .assertNext(cancelado -> assertThat(cancelado.getLineas()).hasSize(1))
        .verifyComplete();
  }
}
