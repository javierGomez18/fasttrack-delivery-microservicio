package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.LineaPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.LineaPedidoRS;
import org.mapstruct.Mapper;

/** Mapper: LineaPedido (Domain) -> LineaPedidoRS (DTO) */
@Mapper(componentModel = "spring")
public interface LineaPedidoResponseMapper {

  LineaPedidoRS toResponse(LineaPedido domain);

  List<LineaPedidoRS> toResponseList(List<LineaPedido> domains);
}
