package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.EventoSeguimiento;
import org.javierGomez18.fasttrack.delivery.microservicio.web.dto.EventoSeguimientoRS;
import org.mapstruct.Mapper;

/** Mapper: EventoSeguimiento (Domain) -> EventoSeguimientoRS (DTO) */
@Mapper(
    componentModel = "spring",
    uses = {DateTimeMapper.class})
public interface EventoSeguimientoResponseMapper {

  EventoSeguimientoRS toResponse(EventoSeguimiento domain);

  List<EventoSeguimientoRS> toResponseList(List<EventoSeguimiento> domains);
}
