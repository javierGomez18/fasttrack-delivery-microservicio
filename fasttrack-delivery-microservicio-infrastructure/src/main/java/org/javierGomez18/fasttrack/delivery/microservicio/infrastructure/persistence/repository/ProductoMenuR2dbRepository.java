package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.repository;

import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity.ProductoMenuEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProductoMenuR2dbRepository extends ReactiveCrudRepository<ProductoMenuEntity, Long> {

    Flux<ProductoMenuEntity> findByRestauranteId(Long restauranteId);
}
