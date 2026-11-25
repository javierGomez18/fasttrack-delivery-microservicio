package org.javierGomez18.fasttrack.delivery.microservicio.domain.model;

import java.math.BigDecimal;
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
public class LineaPedido {
  private Long id;
  private Long pedidoId;
  private Long productoId;
  private Integer cantidad;
  private BigDecimal precioUnitario;
}
