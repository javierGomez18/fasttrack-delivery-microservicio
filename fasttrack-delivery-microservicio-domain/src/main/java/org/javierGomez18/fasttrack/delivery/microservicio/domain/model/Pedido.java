package org.javierGomez18.fasttrack.delivery.microservicio.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
public class Pedido {
  private Long id;
  private Long restauranteId;
  private Long clienteId;
  private EstadoPedido estado;
  private LocalDateTime fechaCreacion;
  private BigDecimal total;
  private Integer tiempoEstimadoMinutos;
  private List<LineaPedido> lineas;
}
