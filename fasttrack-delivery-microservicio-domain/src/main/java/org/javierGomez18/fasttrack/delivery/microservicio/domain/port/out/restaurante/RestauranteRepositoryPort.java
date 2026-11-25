package org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.restaurante;

import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RestauranteRepositoryPort {

    Mono<Restaurante> createRestaurante(Restaurante restaurante);
    Mono<Restaurante> getRestauranteById(Long id);
    Mono<Boolean> existsByNombre(String nombre);
    Flux<Restaurante> getAllActiveRestaurantes();
    Flux<Restaurante> getAllRestaurantes();
    Mono<Restaurante> updateRestaurante(Restaurante restaurante);
    Mono<Boolean> deleteRestaurante(Long id);
}
