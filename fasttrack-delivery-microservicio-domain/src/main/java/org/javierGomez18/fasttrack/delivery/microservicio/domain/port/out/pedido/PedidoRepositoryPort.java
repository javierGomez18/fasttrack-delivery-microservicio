package org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.pedido;

import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PedidoRepositoryPort {

    Mono<Pedido> getPedidoById(Long id);
    Flux<Pedido> getPedidosByRestauranteId(Long id);
    Flux<Pedido> getAllPedidos();
    Flux<Pedido> getPedidosByClienteId(Long id);
    Mono<Pedido> createPedido(Pedido pedido);
    Mono<Pedido> cancelPedido(Pedido pedido);
}
