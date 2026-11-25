package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.LineaPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.LineaPedidoRQ;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper: LineaPedidoRQ (DTO) -> LineaPedido (Domain).
 *
 * <p>El request solo trae productoId y cantidad: id, pedidoId y precioUnitario quedan sin
 * mapear a propósito, es responsabilidad del caso de uso de creación de pedido resolverlos
 * (precioUnitario a partir del precio vigente del producto, pedidoId una vez persistido el
 * pedido).
 */
@Mapper(componentModel = "spring")
public interface LineaPedidoRequestMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "pedidoId", ignore = true)
  @Mapping(target = "precioUnitario", ignore = true)
  LineaPedido toDomain(LineaPedidoRQ request);

  List<LineaPedido> toDomainList(List<LineaPedidoRQ> requests);
}
