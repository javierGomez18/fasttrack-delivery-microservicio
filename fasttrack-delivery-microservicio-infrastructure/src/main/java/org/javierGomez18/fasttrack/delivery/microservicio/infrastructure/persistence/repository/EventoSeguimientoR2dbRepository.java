package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.repository;

import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity.EventoSeguimientoEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EventoSeguimientoR2dbRepository extends ReactiveCrudRepository<EventoSeguimientoEntity, Long> {
}
