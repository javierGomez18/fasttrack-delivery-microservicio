package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class DateTimeMapper {

  public OffsetDateTime map(LocalDateTime dateTime) {
    return dateTime != null ? dateTime.atOffset(ZoneOffset.UTC) : null;
  }

  public LocalDateTime map(OffsetDateTime dateTime) {
    return dateTime != null ? dateTime.toLocalDateTime() : null;
  }
}
