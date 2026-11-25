package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.rest.exception;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.lineapedido.LineaPedidoUnprocessableEntityException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoUnprocessableEntity;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.productomenu.ProductoMenuConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.productomenu.ProductoMenuNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteUnprocessableEntityException;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.ProblemDetailRS;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RestauranteNotFoundException.class)
  public Mono<ResponseEntity<ProblemDetailRS>> handleRestauranteNotFoundException(
      RestauranteNotFoundException ex, ServerWebExchange exchange) {
    return Mono.just(
        buildResponse(
            HttpStatus.NOT_FOUND,
            "Restaurante no encontrado",
            "/errors/restaurante-no-encontrado",
            ex.getMessage(),
            exchange.getRequest()));
  }

  @ExceptionHandler(PedidoNotFoundException.class)
  public Mono<ResponseEntity<ProblemDetailRS>> handlePedidoNotFoundException(
      PedidoNotFoundException ex, ServerWebExchange exchange) {
    return Mono.just(
        buildResponse(
            HttpStatus.NOT_FOUND,
            "Pedido no encontrado",
            "/errors/pedido-no-encontrado",
            ex.getMessage(),
            exchange.getRequest()));
  }

  @ExceptionHandler(ProductoMenuNotFoundException.class)
  public Mono<ResponseEntity<ProblemDetailRS>> handleProductoMenuNotFoundException(
      ProductoMenuNotFoundException ex, ServerWebExchange exchange) {
    return Mono.just(
        buildResponse(
            HttpStatus.NOT_FOUND,
            "Producto de menú no encontrado",
            "/errors/producto-menu-no-encontrado",
            ex.getMessage(),
            exchange.getRequest()));
  }

  @ExceptionHandler(RestauranteConflictException.class)
  public Mono<ResponseEntity<ProblemDetailRS>> handleRestauranteConflictException(
      RestauranteConflictException ex, ServerWebExchange exchange) {
    return Mono.just(
        buildResponse(
            HttpStatus.CONFLICT,
            "Conflicto con el restaurante",
            "/errors/restaurante-conflicto",
            ex.getMessage(),
            exchange.getRequest()));
  }

  @ExceptionHandler(ProductoMenuConflictException.class)
  public Mono<ResponseEntity<ProblemDetailRS>> handleProductoMenuConflictException(
      ProductoMenuConflictException ex, ServerWebExchange exchange) {
    String type =
        switch (ex.getTipo()) {
          case PRODUCTO_MENU_DUPLICADO -> "/errors/producto-menu-duplicado";
        };
    return Mono.just(
        buildResponse(
            HttpStatus.CONFLICT,
            "Conflicto con el producto del menú",
            type,
            ex.getMessage(),
            exchange.getRequest()));
  }

  @ExceptionHandler(PedidoConflictException.class)
  public Mono<ResponseEntity<ProblemDetailRS>> handlePedidoConflictException(
      PedidoConflictException ex, ServerWebExchange exchange) {
    String type =
        switch (ex.getTipo()) {
          case PEDIDO_NOT_CANCELABLE -> "/errors/pedido-no-cancelable";
        };
    return Mono.just(
        buildResponse(
            HttpStatus.CONFLICT,
            "Pedido no cancelable",
            type,
            ex.getMessage(),
            exchange.getRequest()));
  }

  @ExceptionHandler(PedidoUnprocessableEntity.class)
  public Mono<ResponseEntity<ProblemDetailRS>> handlePedidoUnprocessableEntity(
      PedidoUnprocessableEntity ex, ServerWebExchange exchange) {
    String type =
        switch (ex.getTipo()) {
          case PEDIDO_SIN_LINEAS -> "/errors/pedido-sin-lineas";
        };
    return Mono.just(
        buildResponse(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Pedido no procesable",
            type,
            ex.getMessage(),
            exchange.getRequest()));
  }

  @ExceptionHandler(RestauranteUnprocessableEntityException.class)
  public Mono<ResponseEntity<ProblemDetailRS>> handleRestauranteUnprocessableEntityException(
      RestauranteUnprocessableEntityException ex, ServerWebExchange exchange) {
    String type =
        switch (ex.getTipo()) {
          case RESTAURANTE_INACTIVO -> "/errors/restaurante-inactivo";
        };
    return Mono.just(
        buildResponse(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Restaurante no procesable",
            type,
            ex.getMessage(),
            exchange.getRequest()));
  }

  @ExceptionHandler(LineaPedidoUnprocessableEntityException.class)
  public Mono<ResponseEntity<ProblemDetailRS>> handleLineaPedidoUnprocessableEntityException(
      LineaPedidoUnprocessableEntityException ex, ServerWebExchange exchange) {
    String type =
        switch (ex.getTipo()) {
          case PRODUCTO_NO_DISPONIBLE -> "/errors/producto-no-disponible";
          case PRODUCTO_NO_PERTENECE_A_RESTAURANTE -> "/errors/producto-no-pertenece-a-restaurante";
        };
    return Mono.just(
        buildResponse(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Línea de pedido no procesable",
            type,
            ex.getMessage(),
            exchange.getRequest()));
  }

  /**
   * Errores propios de WebFlux (validación del body, parámetros mal formados, ruta inexistente,
   * método no soportado...). Conservan su status original en vez de caer en el 500 genérico.
   */
  @ExceptionHandler(ResponseStatusException.class)
  public Mono<ResponseEntity<ProblemDetailRS>> handleResponseStatusException(
      ResponseStatusException ex, ServerWebExchange exchange) {
    HttpStatusCode status = ex.getStatusCode();
    boolean badRequest = status.value() == HttpStatus.BAD_REQUEST.value();
    String detail =
        ex instanceof WebExchangeBindException bindEx
            ? bindEx.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "))
            : ex.getReason();
    return Mono.just(
        buildResponse(
            status,
            badRequest ? "Petición inválida" : HttpStatus.valueOf(status.value()).getReasonPhrase(),
            badRequest ? "/errors/peticion-invalida" : "/errors/error-http",
            detail,
            exchange.getRequest()));
  }

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<ProblemDetailRS>> handleGenericException(
      Exception ex, ServerWebExchange exchange) {
    log.error("Excepción no controlada en {}", exchange.getRequest().getPath(), ex);
    return Mono.just(
        buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Excepción inesperada",
            "/errors/error-interno",
            "Se ha producido un error interno",
            exchange.getRequest()));
  }

  private ResponseEntity<ProblemDetailRS> buildResponse(
      HttpStatusCode status, String title, String type, String detail, ServerHttpRequest request) {
    ProblemDetailRS body = new ProblemDetailRS();
    body.setType(URI.create(type));
    body.setTitle(title);
    body.setStatus(status.value());
    body.setDetail(detail);
    body.setInstance(URI.create(request.getPath().value()));
    body.setTimestamp(OffsetDateTime.now());
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
  }
}
