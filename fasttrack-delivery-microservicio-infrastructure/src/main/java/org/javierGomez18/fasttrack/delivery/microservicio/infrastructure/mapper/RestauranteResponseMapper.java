package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.RestauranteRS;
import org.mapstruct.Mapper;

/** Mapper: Restaurante (Domain) -> RestauranteRS (DTO) */
@Mapper(componentModel = "spring")
public interface RestauranteResponseMapper {

  RestauranteRS toResponse(Restaurante domain);

  List<RestauranteRS> toResponseList(List<Restaurante> domains);
}
