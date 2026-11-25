package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteConflictException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.restaurante.RestauranteRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.RestauranteEntityMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.repository.RestauranteR2dbRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RestauranteAdapter implements RestauranteRepositoryPort {

    private final RestauranteR2dbRepository restauranteR2dbRepository;
    private final RestauranteEntityMapper restauranteEntityMapper;

    @Override
    public Mono<Restaurante> createRestaurante(Restaurante restaurante) {
        return restauranteR2dbRepository.save(restauranteEntityMapper.toEntity(restaurante))
                .onErrorMap(DataIntegrityViolationException.class,
                        ex -> new RestauranteConflictException(restaurante.getNombre()))
                .map(restauranteEntityMapper::toDomain);
    }

    @Override
    public Mono<Restaurante> getRestauranteById(Long id) {
        return restauranteR2dbRepository.findById(id)
                .map(restauranteEntityMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        return restauranteR2dbRepository.existsByNombre(nombre);
    }

    @Override
    public Flux<Restaurante> getAllActiveRestaurantes() {
        return restauranteR2dbRepository.findByActivoTrue()
                .map(restauranteEntityMapper::toDomain);
    }

    @Override
    public Flux<Restaurante> getAllRestaurantes() {
        return restauranteR2dbRepository.findAll()
                .map(restauranteEntityMapper::toDomain);
    }

    @Override
    public Mono<Restaurante> updateRestaurante(Restaurante restaurante) {
        return restauranteR2dbRepository.findById(restaurante.getId())
                .switchIfEmpty(Mono.error(new RestauranteNotFoundException(restaurante.getId())))
                .flatMap(existingRestaurante -> {
                    existingRestaurante.setNombre(restaurante.getNombre());
                    existingRestaurante.setActivo(restaurante.getActivo());
                    return restauranteR2dbRepository.save(existingRestaurante)
                            .map(restauranteEntityMapper::toDomain);
                });
    }

    @Override
    public Mono<Boolean> deleteRestaurante(Long id) {
        return restauranteR2dbRepository.findById(id)
                .switchIfEmpty(Mono.error(new RestauranteNotFoundException(id)))
                .flatMap(restaurante -> restauranteR2dbRepository.delete(restaurante)
                        .thenReturn(true));
    }
}
