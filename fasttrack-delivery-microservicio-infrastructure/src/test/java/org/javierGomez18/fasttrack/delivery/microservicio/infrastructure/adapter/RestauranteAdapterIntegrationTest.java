package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.CategoriaRestaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.restaurante.RestauranteRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

class RestauranteAdapterIntegrationTest extends AbstractPostgresIntegrationTest {

  @Autowired RestauranteRepositoryPort port;

  private Restaurante nuevo(String nombre) {
    return new Restaurante(null, nombre, CategoriaRestaurante.PIZZERIA, true, null, null);
  }

  @Test
  void existsByNombreDistingueEntreExistenteYNoExistente() {
    StepVerifier.create(port.existsByNombre("Burger Lab")).expectNext(true).verifyComplete();
    StepVerifier.create(port.existsByNombre("No existe")).expectNext(false).verifyComplete();
  }

  @Test
  void getAllActiveRestaurantesExcluyeLosInactivos() {
    StepVerifier.create(port.getAllActiveRestaurantes().map(Restaurante::getNombre).collectList())
        .assertNext(
            nombres -> {
              assertThat(nombres)
                  .contains("Burger Lab", "Sakura Sushi", "Pizzeria Napoli", "Smash House");
              assertThat(nombres).doesNotContain("Trattoria Cerrada");
            })
        .verifyComplete();
  }

  @Test
  void getAllRestaurantesIncluyeLosInactivos() {
    StepVerifier.create(port.getAllRestaurantes().map(Restaurante::getNombre).collectList())
        .assertNext(nombres -> assertThat(nombres).contains("Trattoria Cerrada"))
        .verifyComplete();
  }

  @Test
  void getRestauranteByIdMapeaTodosLosCampos() {
    Long id = restauranteId("Sakura Sushi");

    StepVerifier.create(port.getRestauranteById(id))
        .assertNext(
            r -> {
              assertThat(r.getNombre()).isEqualTo("Sakura Sushi");
              assertThat(r.getCategoria()).isEqualTo(CategoriaRestaurante.SUSHI);
              assertThat(r.getActivo()).isTrue();
            })
        .verifyComplete();
  }

  @Test
  void getRestauranteByIdInexistenteCompletaVacioSinError() {
    // el adapter no decide qué es un 404: eso lo hace el service con switchIfEmpty
    StepVerifier.create(port.getRestauranteById(Long.MAX_VALUE)).verifyComplete();
  }

  @Test
  void createRestauranteGeneraIdYPersiste() {
    String nombre = unico("Pizza");

    StepVerifier.create(port.createRestaurante(nuevo(nombre)))
        .assertNext(
            creado -> {
              assertThat(creado.getId()).isNotNull();
              assertThat(creado.getNombre()).isEqualTo(nombre);
              assertThat(creado.getActivo()).isTrue();
            })
        .verifyComplete();

    StepVerifier.create(port.existsByNombre(nombre)).expectNext(true).verifyComplete();
  }

  @Test
  void createRestauranteConNombreRepetidoTraduceLaConstraintAConflictException() {
    // Aquí se prueba la red de seguridad real: la UNIQUE de Postgres + onErrorMap del adapter,
    // sin pasar por la comprobación previa existsByNombre del service.
    String nombre = unico("Duplicado");
    port.createRestaurante(nuevo(nombre)).block();

    StepVerifier.create(port.createRestaurante(nuevo(nombre)))
        .expectError(RestauranteConflictException.class)
        .verify();
  }
}
