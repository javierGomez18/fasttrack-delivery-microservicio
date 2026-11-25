package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.RestauranteRQ;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapper: RestauranteRQ (DTO) -> Restaurante (Domain) */
@Mapper(componentModel = "spring")
public interface RestauranteRequestMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "activo", constant = "true")
  @Mapping(target = "productoMenuLst", ignore = true)
  @Mapping(target = "pedidosLst", ignore = true)
  Restaurante toDomain(RestauranteRQ request);
}
