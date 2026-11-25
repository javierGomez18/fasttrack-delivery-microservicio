package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.CancelPedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.CreatePedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.FindPedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.PedidoRequestMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.PedidoResponseMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.web.api.PedidosApi;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.EventoSeguimientoRS;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.PedidoRQ;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.PedidoRS;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PedidosController implements PedidosApi {

    private final FindPedidoUseCase findPedidoUseCase;
    private final CreatePedidoUseCase createPedidoUseCase;
    private final CancelPedidoUseCase cancelPedidoUseCase;

    private final PedidoRequestMapper pedidoRequestMapper;
    private final PedidoResponseMapper pedidoResponseMapper;

    @Override
    public Mono<ResponseEntity<Void>> cancelarPedido(Long idPedido, ServerWebExchange exchange) {
        return cancelPedidoUseCase.cancelPedido(idPedido)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<PedidoRS>> createPedido(Mono<PedidoRQ> pedidoRQ, ServerWebExchange exchange) {
        return pedidoRQ.map(pedidoRequestMapper::toDomain)
                .flatMap(createPedidoUseCase::createPedido)
                .map(pedidoResponseMapper::toResponse)
                .map(pedidoRS ->
                        ResponseEntity.status(HttpStatus.CREATED).body(pedidoRS));
    }

    @Override
    public Mono<ResponseEntity<PedidoRS>> findPedidoById(Long idPedido, ServerWebExchange exchange) {
        return findPedidoUseCase.getPedidoById(idPedido)
                .map(pedidoResponseMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<PedidoRS>>> findPedidosByClienteId(Long clienteId, ServerWebExchange exchange) {
        return findPedidoUseCase.getPedidosByClienteId(clienteId)
                .map(pedidoResponseMapper::toResponse)
                .collectList()
                .map(pedidoList ->
                        ResponseEntity.ok(Flux.fromIterable(pedidoList)));
    }
}
