package org.javierGomez18.fasttrack.delivery.microservicio.application.find.pedido;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.FindPedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.pedido.PedidoRepositoryPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class FindPedidoService implements FindPedidoUseCase {

    private final PedidoRepositoryPort pedidoRepositoryPort;

    @Override
    public Mono<Pedido> getPedidoById(Long id) {
        return pedidoRepositoryPort.getPedidoById(id)
                .switchIfEmpty(Mono.error(new PedidoNotFoundException(id)));
    }

    @Override
    public Flux<Pedido> getPedidosByClienteId(Long clientId) {
        return pedidoRepositoryPort.getPedidosByClienteId(clientId);
    }
}
