package org.javierGomez18.fasttrack.delivery.microservicio.domain.model;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventoSeguimiento {
  private Long id;
  private Long pedidoId;
  private EstadoPedido tipo;
  private LocalDateTime timestamp;
  private Map<String, Object> metadata;
}
