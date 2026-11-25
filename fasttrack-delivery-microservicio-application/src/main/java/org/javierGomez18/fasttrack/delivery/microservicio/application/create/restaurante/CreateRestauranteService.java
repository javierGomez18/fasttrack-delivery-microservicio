package org.javierGomez18.fasttrack.delivery.microservicio.application.create.restaurante;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.restaurante.CreateRestauranteUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.productomenu.ProductoMenuRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.restaurante.RestauranteRepositoryPort;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CreateRestauranteService implements CreateRestauranteUseCase {

    private final RestauranteRepositoryPort restauranteRepositoryPort;
    private final ProductoMenuRepositoryPort productoMenuRepositoryPort;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<Restaurante> createRestaurante(Restaurante restaurante) {
        return restauranteRepositoryPort.existsByNombre(restaurante.getNombre())
                .flatMap(exists -> exists ?
                        Mono.<Restaurante>error(new RestauranteConflictException(restaurante.getNombre()))
                        : restauranteRepositoryPort.createRestaurante(restaurante))
                .flatMap(createdRestaurante -> {
                    List<ProductoMenu> productoMenuLst = restaurante.getProductoMenuLst();
                    if(productoMenuLst == null || productoMenuLst.isEmpty()){
                        return Mono.just(createdRestaurante);
                    }
                    productoMenuLst.forEach(productoMenu -> productoMenu.setRestauranteId(createdRestaurante.getId()));
                    return productoMenuRepositoryPort.createProductoMenuLst(productoMenuLst)
                            .collectList()
                            .map(savedProductoMenuLst -> {
                                createdRestaurante.setProductoMenuLst(savedProductoMenuLst);
                                return createdRestaurante;
                            });
                }).as(transactionalOperator::transactional);
    }
}
