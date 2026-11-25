package org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido;

import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import reactor.core.publisher.Mono;

public interface CreatePedidoUseCase {

    Mono<Pedido> createPedido(Pedido pedido);
}
