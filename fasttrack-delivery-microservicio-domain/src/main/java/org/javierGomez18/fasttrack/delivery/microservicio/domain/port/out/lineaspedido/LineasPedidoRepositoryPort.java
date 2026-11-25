package org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.lineaspedido;

import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.LineaPedido;
import reactor.core.publisher.Flux;

import java.util.List;

public interface LineasPedidoRepositoryPort {

    Flux<LineaPedido> getLineasPedidosByPedidoId(Long id);
    Flux<LineaPedido> createLineaPedidoLst(List<LineaPedido> lineasPedidoLst);
}
