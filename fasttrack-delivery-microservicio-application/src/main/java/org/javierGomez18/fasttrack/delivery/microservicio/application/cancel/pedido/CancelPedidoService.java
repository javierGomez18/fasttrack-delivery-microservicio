package org.javierGomez18.fasttrack.delivery.microservicio.application.cancel.pedido;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.EstadoPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.CancelPedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.pedido.PedidoRepositoryPort;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class CancelPedidoService implements CancelPedidoUseCase {

    private final PedidoRepositoryPort pedidoRepositoryPort;

    @Override
    public Mono<Pedido> cancelPedido(Long id) {
        return pedidoRepositoryPort.getPedidoById(id)
                .switchIfEmpty(Mono.error(new PedidoNotFoundException(id)))
                .filter(pedido -> pedido.getEstado() != EstadoPedido.CANCELADO
                        && pedido.getEstado() != EstadoPedido.ENTREGADO)
                .switchIfEmpty(Mono.error(new PedidoConflictException(id)))
                .flatMap(pedido -> {
                    pedido.setEstado(EstadoPedido.CANCELADO);
                    return pedidoRepositoryPort.cancelPedido(pedido);
                });
    }
}
