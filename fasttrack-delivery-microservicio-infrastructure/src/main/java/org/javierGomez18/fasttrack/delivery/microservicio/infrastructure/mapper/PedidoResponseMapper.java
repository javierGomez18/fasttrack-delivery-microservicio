package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.PedidoRS;
import org.mapstruct.Mapper;

/** Mapper: Pedido (Domain) -> PedidoRS (DTO) */
@Mapper(
    componentModel = "spring",
    uses = {LineaPedidoResponseMapper.class, DateTimeMapper.class})
public interface PedidoResponseMapper {

  PedidoRS toResponse(Pedido domain);

  List<PedidoRS> toResponseList(List<Pedido> domains);
}
