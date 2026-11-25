package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.support.AbstractPostgresIntegrationTest;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.PedidoRS;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.RestauranteRS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Test de extremo a extremo: peticiones HTTP reales contra la aplicación arrancada (Netty) y un
 * Postgres real. Recorre controller -> mapper -> service -> adapter -> BBDD y vuelta, incluido el
 * formato de los errores.
 *
 * <p>El WebTestClient de @SpringBootTest ya antepone spring.webflux.base-path (/api/v1) a su URL
 * base, así que las peticiones usan rutas relativas. Solo el campo "instance" de los errores
 * (que es la ruta completa recibida por el servidor) lleva el prefijo.
 */
class FastTrackApiIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final String API = "";
  private static final String BASE_PATH = "/api/v1";
  /** Cada test que crea pedidos usa un cliente propio para no ver los pedidos de otros tests. */
  private static final AtomicLong CLIENTES = new AtomicLong(1_000);

  @Autowired WebTestClient webTestClient;

  // ------------------------------------------------------------------ helpers

  private WebTestClient.ResponseSpec crearPedido(
      Long restauranteId, Long clienteId, String lineasJson) {
    return webTestClient
        .post()
        .uri(API + "/pedidos")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            "{\"restauranteId\":%d,\"clienteId\":%d,\"lineas\":%s}"
                .formatted(restauranteId, clienteId, lineasJson))
        .exchange();
  }

  private String linea(Long productoId, int cantidad) {
    return "{\"productoId\":%d,\"cantidad\":%d}".formatted(productoId, cantidad);
  }

  private PedidoRS cuerpoPedido(WebTestClient.ResponseSpec response) {
    return response.expectBody(PedidoRS.class).returnResult().getResponseBody();
  }

  private List<PedidoRS> pedidosDelCliente(Long clienteId) {
    return webTestClient
        .get()
        .uri(API + "/pedidos?clienteId=" + clienteId)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(PedidoRS.class)
        .returnResult()
        .getResponseBody();
  }

  // ------------------------------------------------------------- restaurantes

  @Test
  void listaSoloLosRestaurantesActivos() {
    List<RestauranteRS> restaurantes =
        webTestClient
            .get()
            .uri(API + "/restaurantes")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(RestauranteRS.class)
            .returnResult()
            .getResponseBody();

    assertThat(restaurantes).extracting(RestauranteRS::getNombre)
        .contains("Burger Lab", "Sakura Sushi", "Pizzeria Napoli", "Smash House")
        .doesNotContain("Trattoria Cerrada");
    assertThat(restaurantes).allSatisfy(r -> assertThat(r.getActivo()).isTrue());
  }

  @Test
  void devuelveElMenuDeUnRestaurante() {
    Long burgerLab = restauranteId("Burger Lab");

    webTestClient
        .get()
        .uri(API + "/restaurantes/" + burgerLab + "/menu")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(5)
        .jsonPath("$[?(@.nombre=='Classic Burger')].precio").isEqualTo(9.5)
        .jsonPath("$[?(@.nombre=='Burger Vegana')].disponible").isEqualTo(false);
  }

  @Test
  void menuDeRestauranteInexistenteDevuelve404() {
    webTestClient
        .get()
        .uri(API + "/restaurantes/999999999/menu")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.title").isEqualTo("Restaurante no encontrado")
        .jsonPath("$.instance").isEqualTo(BASE_PATH + "/restaurantes/999999999/menu");
  }

  @Test
  void creaUnRestauranteYRechazaElDuplicado() {
    String nombre = unico("Nuevo");
    String body = "{\"nombre\":\"%s\",\"categoria\":\"PIZZERIA\"}".formatted(nombre);

    webTestClient
        .post()
        .uri(API + "/restaurantes")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.id").isNumber()
        .jsonPath("$.nombre").isEqualTo(nombre)
        .jsonPath("$.activo").isEqualTo(true);

    webTestClient
        .post()
        .uri(API + "/restaurantes")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectStatus()
        .isEqualTo(409)
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.title").isEqualTo("Conflicto con el restaurante");
  }

  @Test
  void elRestauranteNuevoApareceEnElListado() {
    String nombre = unico("Visible");
    webTestClient
        .post()
        .uri(API + "/restaurantes")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"nombre\":\"%s\",\"categoria\":\"SUSHI\"}".formatted(nombre))
        .exchange()
        .expectStatus()
        .isCreated();

    webTestClient
        .get()
        .uri(API + "/restaurantes")
        .exchange()
        .expectBody()
        .jsonPath("$[?(@.nombre=='%s')]".formatted(nombre)).exists();
  }

  // ------------------------------------------------------------ ciclo de un pedido

  @Test
  void cicloCompletoCrearConsultarListarYCancelar() {
    Long burgerLab = restauranteId("Burger Lab");
    Long classic = productoId(burgerLab, "Classic Burger");
    Long patatas = productoId(burgerLab, "Patatas fritas");
    Long cliente = CLIENTES.incrementAndGet();

    // 1) crear: precios y total los pone el servidor, no el cliente
    PedidoRS creado =
        cuerpoPedido(
            crearPedido(burgerLab, cliente, "[" + linea(classic, 2) + "," + linea(patatas, 1) + "]")
                .expectStatus()
                .isCreated());
    assertThat(creado.getId()).isNotNull();
    assertThat(creado.getEstado().getValue()).isEqualTo("PEDIDO_RECIBIDO");
    assertThat(creado.getTotal()).isEqualTo(22.5); // 2 x 9.50 + 1 x 3.50
    assertThat(creado.getTiempoEstimadoMinutos()).isPositive();
    assertThat(creado.getFechaCreacion()).isNotNull();
    assertThat(creado.getLineas()).hasSize(2);
    assertThat(creado.getLineas())
        .extracting(l -> l.getPrecioUnitario())
        .containsExactlyInAnyOrder(9.5, 3.5);

    // 2) consultarlo por id: lo devuelto coincide con lo guardado
    webTestClient
        .get()
        .uri(API + "/pedidos/" + creado.getId())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo(creado.getId().intValue())
        .jsonPath("$.clienteId").isEqualTo(cliente.intValue())
        .jsonPath("$.total").isEqualTo(22.5)
        .jsonPath("$.lineas.length()").isEqualTo(2);

    // 3) aparece en el listado de su cliente
    assertThat(pedidosDelCliente(cliente)).extracting(PedidoRS::getId).containsExactly(creado.getId());

    // 4) cancelarlo: 204 sin cuerpo, y queda CANCELADO conservando las líneas
    webTestClient
        .patch()
        .uri(API + "/pedidos/" + creado.getId() + "/cancelar")
        .exchange()
        .expectStatus()
        .isNoContent()
        .expectBody()
        .isEmpty();
    webTestClient
        .get()
        .uri(API + "/pedidos/" + creado.getId())
        .exchange()
        .expectBody()
        .jsonPath("$.estado").isEqualTo("CANCELADO")
        .jsonPath("$.lineas.length()").isEqualTo(2);

    // 5) cancelarlo otra vez: ya no se puede
    webTestClient
        .patch()
        .uri(API + "/pedidos/" + creado.getId() + "/cancelar")
        .exchange()
        .expectStatus()
        .isEqualTo(409)
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.title").isEqualTo("Pedido no cancelable");
  }

  @Test
  void elPrecioDelClienteNoSeAceptaYSeIgnora() {
    Long burgerLab = restauranteId("Burger Lab");
    Long classic = productoId(burgerLab, "Classic Burger");

    // el cliente intenta colar un precio y un total propios: el contrato no los admite
    PedidoRS creado =
        cuerpoPedido(
            webTestClient
                .post()
                .uri(API + "/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                    "{\"restauranteId\":%d,\"clienteId\":%d,\"total\":0.01,\"lineas\":[{\"productoId\":%d,\"cantidad\":1,\"precioUnitario\":0.01}]}"
                        .formatted(burgerLab, CLIENTES.incrementAndGet(), classic))
                .exchange()
                .expectStatus()
                .isCreated());

    assertThat(creado.getTotal()).isEqualTo(9.5);
    assertThat(creado.getLineas().get(0).getPrecioUnitario()).isEqualTo(9.5);
  }

  // ----------------------------------------------------- pedidos de data.sql

  @Test
  void elClienteConPedidosDeEjemploLosRecibeConSusLineas() {
    List<PedidoRS> pedidos = pedidosDelCliente(1L);

    assertThat(pedidos).hasSizeGreaterThanOrEqualTo(2);
    assertThat(pedidos).extracting(p -> p.getEstado().getValue())
        .contains("ENTREGADO", "EN_REPARTO");
    assertThat(pedidos).allSatisfy(p -> assertThat(p.getLineas()).hasSize(2));
  }

  @Test
  void clienteSinPedidosRecibeArrayVacioY200() {
    webTestClient
        .get()
        .uri(API + "/pedidos?clienteId=999999999")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("[]");
  }

  @Test
  void noSePuedeCancelarUnPedidoYaEntregado() {
    Long entregado =
        pedidosDelCliente(1L).stream()
            .filter(p -> "ENTREGADO".equals(p.getEstado().getValue()))
            .findFirst()
            .orElseThrow()
            .getId();

    webTestClient
        .patch()
        .uri(API + "/pedidos/" + entregado + "/cancelar")
        .exchange()
        .expectStatus()
        .isEqualTo(409);
  }

  @Test
  void noSePuedeCancelarUnPedidoYaCancelado() {
    Long cancelado =
        pedidosDelCliente(3L).stream()
            .filter(p -> "CANCELADO".equals(p.getEstado().getValue()))
            .findFirst()
            .orElseThrow()
            .getId();

    webTestClient
        .patch()
        .uri(API + "/pedidos/" + cancelado + "/cancelar")
        .exchange()
        .expectStatus()
        .isEqualTo(409);
  }

  @Test
  void pedidoInexistenteDevuelve404EnConsultaYEnCancelacion() {
    webTestClient
        .get()
        .uri(API + "/pedidos/999999999")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Pedido no encontrado");

    webTestClient
        .patch()
        .uri(API + "/pedidos/999999999/cancelar")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  // ------------------------------------------------- errores al crear un pedido

  @Test
  void restauranteInactivoDa422() {
    Long cerrado = restauranteId("Trattoria Cerrada");
    Long lasana = productoId(cerrado, "Lasaña");

    crearPedido(cerrado, CLIENTES.incrementAndGet(), "[" + linea(lasana, 1) + "]")
        .expectStatus()
        .isEqualTo(422)
        .expectBody()
        .jsonPath("$.title").isEqualTo("Restaurante no procesable");
  }

  @Test
  void productoNoDisponibleDa422() {
    Long burgerLab = restauranteId("Burger Lab");
    Long vegana = productoId(burgerLab, "Burger Vegana");
    Long cliente = CLIENTES.incrementAndGet();

    crearPedido(burgerLab, cliente, "[" + linea(vegana, 1) + "]")
        .expectStatus()
        .isEqualTo(422)
        .expectBody()
        .jsonPath("$.type").isEqualTo("/errors/producto-no-disponible");

    assertThat(pedidosDelCliente(cliente)).isEmpty(); // no se guardó nada
  }

  @Test
  void productoDeOtroRestauranteDa422() {
    Long burgerLab = restauranteId("Burger Lab");
    Long sushiMix = productoId(restauranteId("Sakura Sushi"), "Sushi Mix 12 piezas");

    crearPedido(burgerLab, CLIENTES.incrementAndGet(), "[" + linea(sushiMix, 1) + "]")
        .expectStatus()
        .isEqualTo(422)
        .expectBody()
        .jsonPath("$.type").isEqualTo("/errors/producto-no-pertenece-a-restaurante");
  }

  @Test
  void unaLineaInvalidaImpideGuardarElPedidoEntero() {
    Long burgerLab = restauranteId("Burger Lab");
    Long classic = productoId(burgerLab, "Classic Burger"); // válida
    Long vegana = productoId(burgerLab, "Burger Vegana"); // no disponible
    Long cliente = CLIENTES.incrementAndGet();

    crearPedido(burgerLab, cliente, "[" + linea(classic, 1) + "," + linea(vegana, 1) + "]")
        .expectStatus()
        .isEqualTo(422);

    assertThat(pedidosDelCliente(cliente)).isEmpty();
  }

  @Test
  void restauranteInexistenteDa404() {
    crearPedido(999_999_999L, CLIENTES.incrementAndGet(), "[" + linea(1L, 1) + "]")
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Restaurante no encontrado");
  }

  @Test
  void productoInexistenteDa404() {
    Long burgerLab = restauranteId("Burger Lab");

    crearPedido(burgerLab, CLIENTES.incrementAndGet(), "[" + linea(999_999_999L, 1) + "]")
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Producto de menú no encontrado");
  }

  @Test
  void pedidoSinLineasDa400PorValidacion() {
    Long burgerLab = restauranteId("Burger Lab");

    crearPedido(burgerLab, CLIENTES.incrementAndGet(), "[]")
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Petición inválida");
  }

  @Test
  void cantidadCeroDa400PorValidacion() {
    Long burgerLab = restauranteId("Burger Lab");
    Long classic = productoId(burgerLab, "Classic Burger");

    crearPedido(burgerLab, CLIENTES.incrementAndGet(), "[" + linea(classic, 0) + "]")
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void pedidoSinClienteDa400() {
    webTestClient
        .post()
        .uri(API + "/pedidos")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"restauranteId\":1,\"lineas\":[{\"productoId\":1,\"cantidad\":1}]}")
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void listarPedidosSinClienteIdDa400() {
    webTestClient.get().uri(API + "/pedidos").exchange().expectStatus().isBadRequest();
  }

  @Test
  void rutaInexistenteDa404YNoUn500() {
    // el handler genérico no debe tragarse los errores propios de WebFlux
    webTestClient.get().uri(API + "/no-existe").exchange().expectStatus().isNotFound();
  }

  // ------------------------------------------------------------ infraestructura

  @Test
  void actuatorHealthEstaArriba() {
    webTestClient
        .get()
        .uri(API + "/actuator/health")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status").isEqualTo("UP")
        .jsonPath("$.components.r2dbc.status").isEqualTo("UP");
  }

  @Test
  void swaggerEstaExpuestoYDescribeLosEndpoints() {
    webTestClient
        .get()
        .uri(API + "/v3/api-docs")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.paths['/pedidos']").exists()
        .jsonPath("$.paths['/restaurantes/{idRestaurante}/menu']").exists();
  }
}
