package org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido;

import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FindPedidoUseCase {
    Mono<Pedido> getPedidoById(Long id);
    Flux<Pedido> getPedidosByClienteId(Long clientId);
}
