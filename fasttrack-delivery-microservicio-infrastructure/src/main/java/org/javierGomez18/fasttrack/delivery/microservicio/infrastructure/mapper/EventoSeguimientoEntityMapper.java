package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.util.List;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.EventoSeguimiento;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity.EventoSeguimientoEntity;
import org.mapstruct.Mapper;

/** Mapper: EventoSeguimientoEntity <-> EventoSeguimiento (Domain) */
@Mapper(
    componentModel = "spring",
    uses = {MetadataMapper.class})
public interface EventoSeguimientoEntityMapper {

  EventoSeguimiento toDomain(EventoSeguimientoEntity entity);

  EventoSeguimientoEntity toEntity(EventoSeguimiento domain);

  List<EventoSeguimiento> toDomainList(List<EventoSeguimientoEntity> entities);
}
