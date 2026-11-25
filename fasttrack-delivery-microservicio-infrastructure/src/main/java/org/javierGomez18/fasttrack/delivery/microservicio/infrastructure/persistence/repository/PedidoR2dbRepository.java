package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.repository;

import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity.PedidoEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PedidoR2dbRepository extends ReactiveCrudRepository<PedidoEntity, Long> {

    Flux<PedidoEntity> findByRestauranteId(Long restauranteId);
    Flux<PedidoEntity> findByClienteId(Long clienteId);
}
