package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.productomenu.ProductoMenuConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.CategoriaRestaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.productomenu.ProductoMenuRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.restaurante.RestauranteRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

class ProductoMenuAdapterIntegrationTest extends AbstractPostgresIntegrationTest {

  @Autowired ProductoMenuRepositoryPort port;
  @Autowired RestauranteRepositoryPort restauranteRepositoryPort;

  @Test
  void getByRestauranteIdDevuelveSoloElMenuDeEseRestaurante() {
    Long burgerLab = restauranteId("Burger Lab");

    StepVerifier.create(port.getByRestauranteId(burgerLab).collectList())
        .assertNext(
            menu -> {
              assertThat(menu).hasSize(5);
              assertThat(menu).allSatisfy(p -> assertThat(p.getRestauranteId()).isEqualTo(burgerLab));
              assertThat(menu)
                  .extracting(ProductoMenu::getNombre)
                  .contains("Classic Burger", "Burger Vegana");
            })
        .verifyComplete();
  }

  @Test
  void getByRestauranteIdConservaPrecioYDisponibilidad() {
    Long burgerLab = restauranteId("Burger Lab");

    StepVerifier.create(port.getByRestauranteId(burgerLab).collectList())
        .assertNext(
            menu -> {
              ProductoMenu classic = porNombre(menu, "Classic Burger");
              assertThat(classic.getPrecio()).isEqualByComparingTo("9.50");
              assertThat(classic.getDisponible()).isTrue();
              assertThat(porNombre(menu, "Burger Vegana").getDisponible()).isFalse();
            })
        .verifyComplete();
  }

  @Test
  void getByRestauranteIdDeRestauranteSinProductosCompletaVacio() {
    Restaurante sinMenu =
        restauranteRepositoryPort
            .createRestaurante(
                new Restaurante(null, unico("SinMenu"), CategoriaRestaurante.SUSHI, true, null, null))
            .block();

    StepVerifier.create(port.getByRestauranteId(sinMenu.getId())).verifyComplete();
  }

  @Test
  void getByIdsDevuelveSoloLosQueExisten() {
    Long burgerLab = restauranteId("Burger Lab");
    Long classic = productoId(burgerLab, "Classic Burger");

    // 999999999 no existe: no da error, simplemente no aparece (el service decide si es un 404)
    StepVerifier.create(port.getByIds(List.of(classic, 999_999_999L)).collectList())
        .assertNext(
            productos -> {
              assertThat(productos).hasSize(1);
              assertThat(productos.get(0).getId()).isEqualTo(classic);
            })
        .verifyComplete();
  }

  @Test
  void createProductoMenuLstPersisteConIdYPrecioExacto() {
    Restaurante r =
        restauranteRepositoryPort
            .createRestaurante(
                new Restaurante(null, unico("ConMenu"), CategoriaRestaurante.PIZZERIA, true, null, null))
            .block();
    ProductoMenu producto = new ProductoMenu(null, r.getId(), "Margarita XL", new BigDecimal("12.35"), true);

    StepVerifier.create(port.createProductoMenuLst(List.of(producto)).collectList())
        .assertNext(
            guardados -> {
              assertThat(guardados).hasSize(1);
              assertThat(guardados.get(0).getId()).isNotNull();
              assertThat(guardados.get(0).getPrecio()).isEqualByComparingTo("12.35");
            })
        .verifyComplete();
  }

  @Test
  void createProductoMenuLstConNombreRepetidoEnElRestauranteDaConflict() {
    Long burgerLab = restauranteId("Burger Lab");
    // "Classic Burger" ya existe en Burger Lab (UNIQUE restaurante_id + nombre)
    ProductoMenu repetido =
        new ProductoMenu(null, burgerLab, "Classic Burger", new BigDecimal("1.00"), true);

    StepVerifier.create(port.createProductoMenuLst(List.of(repetido)))
        .expectError(ProductoMenuConflictException.class)
        .verify();
  }

  @Test
  void elMismoNombreEnOtroRestauranteNoEsConflicto() {
    // la UNIQUE es (restaurante_id, nombre), no solo nombre
    Long smashHouse = restauranteId("Smash House");
    ProductoMenu mismoNombreOtroLocal =
        new ProductoMenu(null, smashHouse, "Classic Burger", new BigDecimal("8.00"), true);

    StepVerifier.create(port.createProductoMenuLst(List.of(mismoNombreOtroLocal)))
        .expectNextCount(1)
        .verifyComplete();
  }

  private ProductoMenu porNombre(List<ProductoMenu> menu, String nombre) {
    return menu.stream().filter(p -> p.getNombre().equals(nombre)).findFirst().orElseThrow();
  }
}
