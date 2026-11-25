package org.javierGomez18.fasttrack.delivery.microservicio.application.find.restaurante;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.restaurante.FindRestauranteUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.productomenu.ProductoMenuRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.restaurante.RestauranteRepositoryPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class FindRestauranteService implements FindRestauranteUseCase {

    private final RestauranteRepositoryPort restauranteRepositoryPort;
    private final ProductoMenuRepositoryPort productoMenuRepositoryPort;

    @Override
    public Mono<Restaurante> getRestauranteById(Long id) {
        return restauranteRepositoryPort.getRestauranteById(id)
                .switchIfEmpty(Mono.error(new RestauranteNotFoundException(id)));
    }

    @Override
    public Flux<Restaurante> getAllActiveRestaurantes() {
        return restauranteRepositoryPort.getAllActiveRestaurantes();
    }

    @Override
    public Flux<ProductoMenu> getProductoMenuByRestauranteId(Long id) {
        return restauranteRepositoryPort.getRestauranteById(id)
                .switchIfEmpty(Mono.error(new RestauranteNotFoundException(id)))
                .flatMapMany(restaurante -> productoMenuRepositoryPort.getByRestauranteId(restaurante.getId()));
    }
}
