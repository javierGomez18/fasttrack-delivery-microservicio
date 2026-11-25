package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.ProductoMenuRS;
import org.mapstruct.Mapper;

/** Mapper: ProductoMenu (Domain) -> ProductoMenuRS (DTO) */
@Mapper(componentModel = "spring")
public interface ProductoMenuResponseMapper {

  ProductoMenuRS toResponse(ProductoMenu domain);

  List<ProductoMenuRS> toResponseList(List<ProductoMenu> domains);
}
