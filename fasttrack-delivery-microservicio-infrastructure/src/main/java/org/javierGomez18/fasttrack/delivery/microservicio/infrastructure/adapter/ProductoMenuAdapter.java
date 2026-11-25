package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.productomenu.ProductoMenuConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.productomenu.ProductoMenuRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.ProductoMenuEntityMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.repository.ProductoMenuR2dbRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductoMenuAdapter implements ProductoMenuRepositoryPort {

    private final ProductoMenuR2dbRepository productoMenuR2dbRepository;
    private final ProductoMenuEntityMapper productoMenuEntityMapper;

    @Override
    public Flux<ProductoMenu> createProductoMenuLst(List<ProductoMenu> productoMenuLst) {
        return Flux.fromIterable(productoMenuLst)
                .map(productoMenuEntityMapper::toEntity)
                .flatMap(product -> productoMenuR2dbRepository.save(product)
                        .onErrorMap(DataIntegrityViolationException.class,
                                ex -> new ProductoMenuConflictException(product.getNombre()))
                ).map(productoMenuEntityMapper::toDomain);
    }

    @Override
    public Flux<ProductoMenu> getByRestauranteId(Long restauranteId) {
        return productoMenuR2dbRepository.findByRestauranteId(restauranteId)
                .map(productoMenuEntityMapper::toDomain);
    }

    @Override
    public Flux<ProductoMenu> getByIds(List<Long> productoMenuIds) {
        return productoMenuR2dbRepository.findAllById(productoMenuIds)
                .map(productoMenuEntityMapper::toDomain);
    }
}
