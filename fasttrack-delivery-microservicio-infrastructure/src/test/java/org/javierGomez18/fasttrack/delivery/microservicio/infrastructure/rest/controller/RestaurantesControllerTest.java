package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.CategoriaRestaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.restaurante.CreateRestauranteUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.restaurante.FindRestauranteUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.ProductoMenuResponseMapperImpl;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.RestauranteRequestMapperImpl;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.RestauranteResponseMapperImpl;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Test de la capa web aislada: los casos de uso son mocks, los mappers y el GlobalExceptionHandler
 * son los reales. Comprueba rutas, códigos HTTP, validación y formato de errores, sin BBDD.
 */
@WebFluxTest(RestaurantesController.class)
@Import({
  GlobalExceptionHandler.class,
  RestauranteRequestMapperImpl.class,
  RestauranteResponseMapperImpl.class,
  ProductoMenuResponseMapperImpl.class
})
class RestaurantesControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean FindRestauranteUseCase findRestauranteUseCase;
  @MockBean CreateRestauranteUseCase createRestauranteUseCase;

  private Restaurante restaurante(Long id, String nombre, boolean activo) {
    return new Restaurante(id, nombre, CategoriaRestaurante.SUSHI, activo, null, null);
  }

  // ------------------------------------------------------------- POST /restaurantes

  @Test
  void createRestauranteDevuelve201YElMapperLoDaDeAltaComoActivo() {
    when(createRestauranteUseCase.createRestaurante(any(Restaurante.class)))
        .thenReturn(Mono.just(restaurante(7L, "Sushi Zen", true)));

    webTestClient
        .post()
        .uri("/restaurantes")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"nombre\":\"Sushi Zen\",\"categoria\":\"SUSHI\"}")
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.id").isEqualTo(7)
        .jsonPath("$.nombre").isEqualTo("Sushi Zen")
        .jsonPath("$.categoria").isEqualTo("SUSHI")
        .jsonPath("$.activo").isEqualTo(true);

    // lo que le llega al caso de uso: id sin asignar y activo=true (lo fija el mapper, no el cliente)
    ArgumentCaptor<Restaurante> captor = ArgumentCaptor.forClass(Restaurante.class);
    verify(createRestauranteUseCase).createRestaurante(captor.capture());
    assertThat(captor.getValue().getId()).isNull();
    assertThat(captor.getValue().getActivo()).isTrue();
    assertThat(captor.getValue().getCategoria()).isEqualTo(CategoriaRestaurante.SUSHI);
  }

  @Test
  void createRestauranteDuplicadoDevuelve409() {
    when(createRestauranteUseCase.createRestaurante(any(Restaurante.class)))
        .thenReturn(Mono.error(new RestauranteConflictException("Sushi Zen")));

    webTestClient
        .post()
        .uri("/restaurantes")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"nombre\":\"Sushi Zen\",\"categoria\":\"SUSHI\"}")
        .exchange()
        .expectStatus()
        .isEqualTo(409)
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.title").isEqualTo("Conflicto con el restaurante");
  }

  @Test
  void createRestauranteConNombreVacioDevuelve400SinLlegarAlCasoDeUso() {
    webTestClient
        .post()
        .uri("/restaurantes")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"nombre\":\"\",\"categoria\":\"SUSHI\"}")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Petición inválida");

    verifyNoInteractions(createRestauranteUseCase);
  }

  @Test
  void createRestauranteConCategoriaInexistenteDevuelve400() {
    webTestClient
        .post()
        .uri("/restaurantes")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"nombre\":\"Sushi Zen\",\"categoria\":\"MARCIANA\"}")
        .exchange()
        .expectStatus()
        .isBadRequest();

    verifyNoInteractions(createRestauranteUseCase);
  }

  @Test
  void createRestauranteSinCategoriaDevuelve400() {
    webTestClient
        .post()
        .uri("/restaurantes")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"nombre\":\"Sushi Zen\"}")
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  // -------------------------------------------------------------- GET /restaurantes

  @Test
  void findRestaurantesActivosDevuelveElArray() {
    when(findRestauranteUseCase.getAllActiveRestaurantes())
        .thenReturn(Flux.just(restaurante(1L, "Burger Lab", true), restaurante(2L, "Sakura", true)));

    webTestClient
        .get()
        .uri("/restaurantes")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("$[0].nombre").isEqualTo("Burger Lab")
        .jsonPath("$[1].id").isEqualTo(2);
  }

  @Test
  void findRestaurantesActivosSinResultadosDevuelve200ArrayVacio() {
    when(findRestauranteUseCase.getAllActiveRestaurantes()).thenReturn(Flux.empty());

    webTestClient.get().uri("/restaurantes").exchange().expectStatus().isOk().expectBody().json("[]");
  }

  // ------------------------------------------------- GET /restaurantes/{id}/menu

  @Test
  void findMenuDevuelveLosProductos() {
    when(findRestauranteUseCase.getProductoMenuByRestauranteId(1L))
        .thenReturn(
            Flux.just(
                new ProductoMenu(5L, 1L, "Edamame", new BigDecimal("4.50"), true),
                new ProductoMenu(6L, 1L, "Gyoza", new BigDecimal("5.00"), false)));

    webTestClient
        .get()
        .uri("/restaurantes/1/menu")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("$[0].nombre").isEqualTo("Edamame")
        .jsonPath("$[0].precio").isEqualTo(4.5)
        .jsonPath("$[0].restauranteId").isEqualTo(1)
        .jsonPath("$[1].disponible").isEqualTo(false);
  }

  @Test
  void findMenuDeRestauranteSinProductosDevuelve200ArrayVacio() {
    when(findRestauranteUseCase.getProductoMenuByRestauranteId(1L)).thenReturn(Flux.empty());

    webTestClient
        .get()
        .uri("/restaurantes/1/menu")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("[]");
  }

  @Test
  void findMenuDeRestauranteInexistenteDevuelve404ProblemJson() {
    when(findRestauranteUseCase.getProductoMenuByRestauranteId(99L))
        .thenReturn(Flux.error(new RestauranteNotFoundException(99L)));

    webTestClient
        .get()
        .uri("/restaurantes/99/menu")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.title").isEqualTo("Restaurante no encontrado")
        .jsonPath("$.instance").isEqualTo("/restaurantes/99/menu");
  }

  @Test
  void findMenuConIdNoNumericoDevuelve400() {
    webTestClient.get().uri("/restaurantes/abc/menu").exchange().expectStatus().isBadRequest();
  }
}
