package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity.RestauranteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper: RestauranteEntity <-> Restaurante (Domain).
 *
 * <p>productoMenuLst y pedidosLst quedan sin mapear a propósito: R2DBC no tiene relaciones
 * tipo @OneToMany, así que es el service quien las rellena tras consultarlas por separado.
 */
@Mapper(componentModel = "spring")
public interface RestauranteEntityMapper {

  @Mapping(target = "productoMenuLst", ignore = true)
  @Mapping(target = "pedidosLst", ignore = true)
  Restaurante toDomain(RestauranteEntity entity);

  RestauranteEntity toEntity(Restaurante domain);

  List<Restaurante> toDomainList(List<RestauranteEntity> entities);
}
