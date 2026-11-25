package org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.restaurante;

import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import reactor.core.publisher.Mono;

public interface CreateRestauranteUseCase {

    Mono<Restaurante> createRestaurante(Restaurante restaurante);
}
