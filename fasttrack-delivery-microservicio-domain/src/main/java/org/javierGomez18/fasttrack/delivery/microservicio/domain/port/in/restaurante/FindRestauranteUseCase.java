package org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.restaurante;

import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FindRestauranteUseCase {

    Mono<Restaurante> getRestauranteById(Long id);
    Flux<Restaurante> getAllActiveRestaurantes();
    Flux<ProductoMenu> getProductoMenuByRestauranteId(Long id);
}
