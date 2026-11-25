package org.javierGomez18.fasttrack.delivery.microservicio.application.create.restaurante;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.productomenu.ProductoMenuConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.CategoriaRestaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
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
class CreateRestauranteServiceTest {

  @Mock RestauranteRepositoryPort restauranteRepositoryPort;
  @Mock ProductoMenuRepositoryPort productoMenuRepositoryPort;
  @Mock TransactionalOperator transactionalOperator;
  @InjectMocks CreateRestauranteService service;

  @BeforeEach
  void setUp() {
    when(transactionalOperator.transactional(any(Mono.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    // el adapter real devuelve el restaurante guardado con su id y SIN productoMenuLst (el mapper la ignora)
    when(restauranteRepositoryPort.createRestaurante(any(Restaurante.class)))
        .thenAnswer(
            invocation -> {
              Restaurante r = invocation.getArgument(0);
              return Mono.just(
                  new Restaurante(7L, r.getNombre(), r.getCategoria(), r.getActivo(), null, null));
            });
  }

  private Restaurante nuevo(List<ProductoMenu> menu) {
    return new Restaurante(null, "Sushi Zen", CategoriaRestaurante.SUSHI, true, menu, null);
  }

  private ProductoMenu producto(String nombre) {
    return new ProductoMenu(null, null, nombre, new BigDecimal("4.50"), true);
  }

  @Test
  void nombreExistenteLanzaConflictYNoCreaNada() {
    when(restauranteRepositoryPort.existsByNombre("Sushi Zen")).thenReturn(Mono.just(true));

    StepVerifier.create(service.createRestaurante(nuevo(null)))
        .expectError(RestauranteConflictException.class)
        .verify();
    verify(restauranteRepositoryPort, never()).createRestaurante(any());
    verifyNoInteractions(productoMenuRepositoryPort);
  }

  @Test
  void creaElRestauranteSinMenuYNoTocaElPuertoDeProductos() {
    when(restauranteRepositoryPort.existsByNombre("Sushi Zen")).thenReturn(Mono.just(false));

    StepVerifier.create(service.createRestaurante(nuevo(null)))
        .assertNext(
            creado -> {
              assertThat(creado.getId()).isEqualTo(7L);
              assertThat(creado.getNombre()).isEqualTo("Sushi Zen");
            })
        .verifyComplete();
    verifyNoInteractions(productoMenuRepositoryPort);
  }

  @Test
  void menuVacioEquivaleASinMenu() {
    when(restauranteRepositoryPort.existsByNombre("Sushi Zen")).thenReturn(Mono.just(false));

    StepVerifier.create(service.createRestaurante(nuevo(new ArrayList<>())))
        .assertNext(creado -> assertThat(creado.getId()).isEqualTo(7L))
        .verifyComplete();
    verifyNoInteractions(productoMenuRepositoryPort);
  }

  @Test
  void conMenuAsignaElIdDelRestauranteACadaProductoYLosDevuelveGuardados() {
    when(restauranteRepositoryPort.existsByNombre("Sushi Zen")).thenReturn(Mono.just(false));
    when(productoMenuRepositoryPort.createProductoMenuLst(any()))
        .thenAnswer(
            invocation -> {
              List<ProductoMenu> productos = invocation.getArgument(0);
              long id = 100;
              for (ProductoMenu p : productos) {
                p.setId(id++);
              }
              return Flux.fromIterable(productos);
            });

    Restaurante entrada = nuevo(new ArrayList<>(List.of(producto("Edamame"), producto("Gyoza"))));

    StepVerifier.create(service.createRestaurante(entrada))
        .assertNext(
            creado -> {
              assertThat(creado.getProductoMenuLst()).hasSize(2);
              // cada producto cuelga del restaurante recién creado (id 7), no de null
              assertThat(creado.getProductoMenuLst())
                  .allSatisfy(p -> assertThat(p.getRestauranteId()).isEqualTo(7L));
              assertThat(creado.getProductoMenuLst()).extracting(ProductoMenu::getId)
                  .containsExactly(100L, 101L);
            })
        .verifyComplete();
  }

  @Test
  void siFallaElMenuElErrorSePropagaParaQueLaTransaccionHagaRollback() {
    when(restauranteRepositoryPort.existsByNombre("Sushi Zen")).thenReturn(Mono.just(false));
    when(productoMenuRepositoryPort.createProductoMenuLst(any()))
        .thenReturn(Flux.error(new ProductoMenuConflictException("Edamame")));

    StepVerifier.create(service.createRestaurante(nuevo(new ArrayList<>(List.of(producto("Edamame"))))))
        .expectError(ProductoMenuConflictException.class)
        .verify();
  }
}
