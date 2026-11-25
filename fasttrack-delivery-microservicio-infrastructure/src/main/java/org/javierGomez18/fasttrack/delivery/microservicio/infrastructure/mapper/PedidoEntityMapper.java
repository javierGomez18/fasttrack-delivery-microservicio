package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity.PedidoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper: PedidoEntity <-> Pedido (Domain).
 *
 * <p>PedidoEntity no tiene lineas (tabla aparte, ver LineaPedidoEntity): toDomain() deja
 * `lineas` sin mapear a propósito, es el adaptador quien las consulta por separado y las
 * combina con el Pedido ya mapeado.
 */
@Mapper(componentModel = "spring")
public interface PedidoEntityMapper {

  @Mapping(target = "lineas", ignore = true)
  Pedido toDomain(PedidoEntity entity);

  PedidoEntity toEntity(Pedido domain);

  List<Pedido> toDomainList(List<PedidoEntity> entities);
}
