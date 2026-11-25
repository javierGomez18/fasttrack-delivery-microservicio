package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.repository;

import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity.RestauranteEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RestauranteR2dbRepository extends ReactiveCrudRepository<RestauranteEntity, Long> {
    Flux<RestauranteEntity> findByActivoTrue();
    Mono<Boolean> existsByNombre(String nombre);

}
