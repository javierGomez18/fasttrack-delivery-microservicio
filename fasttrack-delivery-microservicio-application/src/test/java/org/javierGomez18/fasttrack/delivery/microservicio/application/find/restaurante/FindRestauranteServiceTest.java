package org.javierGomez18.fasttrack.delivery.microservicio.application.find.restaurante;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.productomenu.ProductoMenuRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.restaurante.RestauranteRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FindRestauranteServiceTest {

  @Mock RestauranteRepositoryPort restauranteRepositoryPort;
  @Mock ProductoMenuRepositoryPort productoMenuRepositoryPort;
  @InjectMocks FindRestauranteService service;

  private Restaurante restaurante(Long id) {
    return new Restaurante(id, "Sushi Zen", null, true, null, null);
  }

  @Test
  void getRestauranteByIdDevuelveElRestaurante() {
    Restaurante r = restaurante(1L);
    when(restauranteRepositoryPort.getRestauranteById(1L)).thenReturn(Mono.just(r));

    StepVerifier.create(service.getRestauranteById(1L)).expectNext(r).verifyComplete();
  }

  @Test
  void getRestauranteByIdInexistenteLanzaNotFound() {
    when(restauranteRepositoryPort.getRestauranteById(99L)).thenReturn(Mono.empty());

    StepVerifier.create(service.getRestauranteById(99L))
        .expectError(RestauranteNotFoundException.class)
        .verify();
  }

  @Test
  void getAllActiveRestaurantesDelegaEnElPuerto() {
    Restaurante r1 = restaurante(1L);
    Restaurante r2 = restaurante(2L);
    when(restauranteRepositoryPort.getAllActiveRestaurantes()).thenReturn(Flux.just(r1, r2));

    StepVerifier.create(service.getAllActiveRestaurantes()).expectNext(r1, r2).verifyComplete();
  }

  @Test
  void getAllActiveRestaurantesSinNingunoCompletaVacioSinError() {
    when(restauranteRepositoryPort.getAllActiveRestaurantes()).thenReturn(Flux.empty());

    StepVerifier.create(service.getAllActiveRestaurantes()).verifyComplete();
  }

  @Test
  void menuDeUnRestauranteExistenteDevuelveSusProductos() {
    ProductoMenu producto = new ProductoMenu(5L, 1L, "Edamame", new BigDecimal("4.50"), true);
    when(restauranteRepositoryPort.getRestauranteById(1L)).thenReturn(Mono.just(restaurante(1L)));
    when(productoMenuRepositoryPort.getByRestauranteId(1L)).thenReturn(Flux.just(producto));

    StepVerifier.create(service.getProductoMenuByRestauranteId(1L))
        .expectNext(producto)
        .verifyComplete();
  }

  @Test
  void menuDeUnRestauranteSinProductosCompletaVacioSinError() {
    // restaurante existe pero no tiene menú: 200 [], no 404
    when(restauranteRepositoryPort.getRestauranteById(1L)).thenReturn(Mono.just(restaurante(1L)));
    when(productoMenuRepositoryPort.getByRestauranteId(1L)).thenReturn(Flux.empty());

    StepVerifier.create(service.getProductoMenuByRestauranteId(1L)).verifyComplete();
  }

  @Test
  void menuDeUnRestauranteInexistenteLanzaNotFoundSinConsultarProductos() {
    when(restauranteRepositoryPort.getRestauranteById(99L)).thenReturn(Mono.empty());

    StepVerifier.create(service.getProductoMenuByRestauranteId(99L))
        .expectError(RestauranteNotFoundException.class)
        .verify();
    verifyNoInteractions(productoMenuRepositoryPort);
  }
}
