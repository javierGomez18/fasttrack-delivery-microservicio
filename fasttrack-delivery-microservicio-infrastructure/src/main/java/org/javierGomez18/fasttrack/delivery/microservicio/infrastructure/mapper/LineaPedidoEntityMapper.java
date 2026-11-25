package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.LineaPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity.LineaPedidoEntity;
import org.mapstruct.Mapper;

/** Mapper: LineaPedidoEntity <-> LineaPedido (Domain) */
@Mapper(componentModel = "spring")
public interface LineaPedidoEntityMapper {

  LineaPedido toDomain(LineaPedidoEntity entity);

  LineaPedidoEntity toEntity(LineaPedido domain);

  List<LineaPedido> toDomainList(List<LineaPedidoEntity> entities);

  List<LineaPedidoEntity> toEntityList(List<LineaPedido> domains);
}
