package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.EstadoPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.LineaPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.CancelPedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.CreatePedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.FindPedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.DateTimeMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.LineaPedidoResponseMapperImpl;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.PedidoRequestMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.PedidoResponseMapperImpl;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.rest.exception.GlobalExceptionHandler;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.PedidoRQ;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(PedidosController.class)
@Import({
  GlobalExceptionHandler.class,
  PedidoResponseMapperImpl.class,
  LineaPedidoResponseMapperImpl.class,
  DateTimeMapper.class
})
class PedidosControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean FindPedidoUseCase findPedidoUseCase;
  @MockBean CreatePedidoUseCase createPedidoUseCase;
  @MockBean CancelPedidoUseCase cancelPedidoUseCase;
  @MockBean PedidoRequestMapper pedidoRequestMapper;

  private Pedido pedido(Long id) {
    return Pedido.builder()
        .id(id)
        .restauranteId(2L)
        .clienteId(3L)
        .estado(EstadoPedido.PEDIDO_RECIBIDO)
        .fechaCreacion(LocalDateTime.of(2026, 10, 6, 13, 20))
        .total(new BigDecimal("13.50"))
        .tiempoEstimadoMinutos(30)
        .lineas(
            List.of(
                LineaPedido.builder()
                    .id(10L)
                    .pedidoId(id)
                    .productoId(5L)
                    .cantidad(3)
                    .precioUnitario(new BigDecimal("4.50"))
                    .build()))
        .build();
  }

  // ---------------------------------------------------------------- findPedidoById

  @Test
  void findPedidoByIdDevuelve200ConSusLineas() {
    when(findPedidoUseCase.getPedidoById(1L)).thenReturn(Mono.just(pedido(1L)));

    webTestClient
        .get()
        .uri("/pedidos/1")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo(1)
        .jsonPath("$.estado").isEqualTo("PEDIDO_RECIBIDO")
        .jsonPath("$.total").isEqualTo(13.5)
        .jsonPath("$.lineas.length()").isEqualTo(1)
        .jsonPath("$.lineas[0].precioUnitario").isEqualTo(4.5);
  }

  @Test
  void findPedidoByIdInexistenteDevuelve404ProblemJson() {
    when(findPedidoUseCase.getPedidoById(99L))
        .thenReturn(Mono.error(new PedidoNotFoundException(99L)));

    webTestClient
        .get()
        .uri("/pedidos/99")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.title").isEqualTo("Pedido no encontrado")
        .jsonPath("$.status").isEqualTo(404)
        .jsonPath("$.instance").isEqualTo("/pedidos/99");
  }

  // ------------------------------------------------------- findPedidosByClienteId

  @Test
  void findPedidosByClienteIdDevuelveArrayConLosPedidos() {
    when(findPedidoUseCase.getPedidosByClienteId(3L))
        .thenReturn(Flux.just(pedido(1L), pedido(2L)));

    webTestClient
        .get()
        .uri("/pedidos?clienteId=3")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("$[1].id").isEqualTo(2)
        .jsonPath("$[0].lineas.length()").isEqualTo(1);
  }

  @Test
  void findPedidosByClienteIdSinPedidosDevuelve200ArrayVacio() {
    when(findPedidoUseCase.getPedidosByClienteId(7L)).thenReturn(Flux.empty());

    webTestClient
        .get()
        .uri("/pedidos?clienteId=7")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("[]");
  }

  @Test
  void findPedidosByClienteIdSinParametroDevuelve400() {
    webTestClient
        .get()
        .uri("/pedidos")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.title").isEqualTo("Petición inválida");
  }

  // ---------------------------------------------------------------- cancelarPedido

  @Test
  void cancelarPedidoDevuelve204() {
    when(cancelPedidoUseCase.cancelPedido(1L)).thenReturn(Mono.just(pedido(1L)));

    webTestClient.patch().uri("/pedidos/1/cancelar").exchange().expectStatus().isNoContent();
  }

  @Test
  void cancelarPedidoYaEntregadoDevuelve409() {
    when(cancelPedidoUseCase.cancelPedido(2L))
        .thenReturn(Mono.error(new PedidoConflictException(2L)));

    webTestClient
        .patch()
        .uri("/pedidos/2/cancelar")
        .exchange()
        .expectStatus()
        .isEqualTo(409)
        .expectBody()
        .jsonPath("$.title").isEqualTo("Pedido no cancelable");
  }

  @Test
  void cancelarPedidoInexistenteDevuelve404() {
    when(cancelPedidoUseCase.cancelPedido(99L))
        .thenReturn(Mono.error(new PedidoNotFoundException(99L)));

    webTestClient.patch().uri("/pedidos/99/cancelar").exchange().expectStatus().isNotFound();
  }

  // ---------------------------------------------------------------- createPedido

  @Test
  void createPedidoDevuelve201SegunElContrato() {
    when(pedidoRequestMapper.toDomain(any(PedidoRQ.class))).thenReturn(pedido(null));
    when(createPedidoUseCase.createPedido(any(Pedido.class))).thenReturn(Mono.just(pedido(100L)));

    webTestClient
        .post()
        .uri("/pedidos")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            "{\"restauranteId\":2,\"clienteId\":3,\"lineas\":[{\"productoId\":5,\"cantidad\":3}]}")
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.id").isEqualTo(100);
  }

  @Test
  void createPedidoSinLineasDevuelve400PorValidacion() {
    webTestClient
        .post()
        .uri("/pedidos")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"restauranteId\":2,\"clienteId\":3,\"lineas\":[]}")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Petición inválida");
  }
}
