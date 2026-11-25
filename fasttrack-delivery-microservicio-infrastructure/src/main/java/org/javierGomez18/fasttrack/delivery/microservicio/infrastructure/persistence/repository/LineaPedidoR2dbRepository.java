package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.repository;

import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity.LineaPedidoEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.Collection;

public interface LineaPedidoR2dbRepository extends ReactiveCrudRepository<LineaPedidoEntity, Long> {
    Flux<LineaPedidoEntity> findByPedidoId(Long pedidoId);
    Flux<LineaPedidoEntity> findByPedidoIdIn(Collection<Long> pedidoIds);
}
