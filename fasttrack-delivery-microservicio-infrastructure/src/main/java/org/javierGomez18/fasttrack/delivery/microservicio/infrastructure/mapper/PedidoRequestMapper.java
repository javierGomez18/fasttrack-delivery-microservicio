package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.PedidoRQ;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper: PedidoRQ (DTO) -> Pedido (Domain).
 *
 * <p>fechaCreacion, total y tiempoEstimadoMinutos quedan sin mapear: los resuelve el caso de
 * uso de creación (fecha actual, suma de las líneas ya con precio resuelto, y llamada al
 * servicio externo de tiempo estimado respectivamente).
 */
@Mapper(
    componentModel = "spring",
    uses = {LineaPedidoRequestMapper.class})
public interface PedidoRequestMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "estado", constant = "PEDIDO_RECIBIDO")
  @Mapping(target = "fechaCreacion", ignore = true)
  @Mapping(target = "total", ignore = true)
  @Mapping(target = "tiempoEstimadoMinutos", ignore = true)
  @Mapping(source = "lineas", target = "lineas")
  Pedido toDomain(PedidoRQ request);
}
