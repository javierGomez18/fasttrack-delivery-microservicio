package org.javierGomez18.fasttrack.delivery.microservicio.application.find.pedido;

import static org.mockito.Mockito.when;

import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.pedido.PedidoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FindPedidoServiceTest {

  @Mock PedidoRepositoryPort pedidoRepositoryPort;
  @InjectMocks FindPedidoService service;

  @Test
  void getPedidoByIdDevuelveElPedido() {
    Pedido pedido = Pedido.builder().id(1L).build();
    when(pedidoRepositoryPort.getPedidoById(1L)).thenReturn(Mono.just(pedido));

    StepVerifier.create(service.getPedidoById(1L)).expectNext(pedido).verifyComplete();
  }

  @Test
  void getPedidoByIdInexistenteLanzaNotFound() {
    // el adapter devuelve vacío cuando no encuentra; es el service quien decide que eso es un 404
    when(pedidoRepositoryPort.getPedidoById(99L)).thenReturn(Mono.empty());

    StepVerifier.create(service.getPedidoById(99L))
        .expectError(PedidoNotFoundException.class)
        .verify();
  }

  @Test
  void getPedidosByClienteIdDevuelveSusPedidos() {
    Pedido p1 = Pedido.builder().id(1L).build();
    Pedido p2 = Pedido.builder().id(2L).build();
    when(pedidoRepositoryPort.getPedidosByClienteId(3L)).thenReturn(Flux.just(p1, p2));

    StepVerifier.create(service.getPedidosByClienteId(3L)).expectNext(p1, p2).verifyComplete();
  }

  @Test
  void getPedidosByClienteIdSinPedidosCompletaVacioSinError() {
    // una lista vacía NO es un error: el controller responderá 200 []
    when(pedidoRepositoryPort.getPedidosByClienteId(7L)).thenReturn(Flux.empty());

    StepVerifier.create(service.getPedidosByClienteId(7L)).verifyComplete();
  }
}
