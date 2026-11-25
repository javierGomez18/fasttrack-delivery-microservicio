package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.restaurante.CreateRestauranteUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.restaurante.FindRestauranteUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.ProductoMenuResponseMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.RestauranteRequestMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.RestauranteResponseMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.web.api.RestaurantesApi;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.ProductoMenuRS;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.RestauranteRQ;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.RestauranteRS;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RestaurantesController implements RestaurantesApi {

    private final FindRestauranteUseCase findRestauranteUseCase;
    private final CreateRestauranteUseCase createRestauranteUseCase;

    private final RestauranteRequestMapper restauranteRequestMapper;
    private final RestauranteResponseMapper restauranteResponseMapper;
    private final ProductoMenuResponseMapper productoMenuResponseMapper;

    @Override
    public Mono<ResponseEntity<RestauranteRS>> createRestaurante(Mono<RestauranteRQ> restauranteRQ, ServerWebExchange exchange) {
        return restauranteRQ.map(restauranteRequestMapper::toDomain)
                .flatMap(createRestauranteUseCase::createRestaurante)
                .map(restauranteResponseMapper::toResponse)
                .map(restauranteRS ->
                        ResponseEntity.status(HttpStatus.CREATED).body(restauranteRS));
    }

    @Override
    public Mono<ResponseEntity<Flux<ProductoMenuRS>>> findMenuByRestauranteId(Long idRestaurante, ServerWebExchange exchange) {
        return findRestauranteUseCase.getProductoMenuByRestauranteId(idRestaurante)
                .map(productoMenuResponseMapper::toResponse)
                .collectList()
                .map(productoMenuList ->
                        ResponseEntity.ok(Flux.fromIterable(productoMenuList)));
    }

    @Override
    public Mono<ResponseEntity<Flux<RestauranteRS>>> findRestaurantesActivos(ServerWebExchange exchange) {
        return findRestauranteUseCase.getAllActiveRestaurantes()
                .map(restauranteResponseMapper::toResponse)
                .collectList()
                .map(restauranteList ->
                        ResponseEntity.ok(Flux.fromIterable(restauranteList)));
    }
}
