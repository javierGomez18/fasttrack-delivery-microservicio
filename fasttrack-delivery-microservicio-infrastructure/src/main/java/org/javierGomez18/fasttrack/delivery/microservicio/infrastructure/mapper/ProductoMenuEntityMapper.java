package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity.ProductoMenuEntity;
import org.mapstruct.Mapper;

/** Mapper: ProductoMenuEntity <-> ProductoMenu (Domain) */
@Mapper(componentModel = "spring")
public interface ProductoMenuEntityMapper {

  ProductoMenu toDomain(ProductoMenuEntity entity);

  ProductoMenuEntity toEntity(ProductoMenu domain);

  List<ProductoMenu> toDomainList(List<ProductoMenuEntity> entities);

  List<ProductoMenuEntity> toEntityList(List<ProductoMenu> domains);
}
