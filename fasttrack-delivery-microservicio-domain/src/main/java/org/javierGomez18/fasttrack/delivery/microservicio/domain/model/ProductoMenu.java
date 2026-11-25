package org.javierGomez18.fasttrack.delivery.microservicio.domain.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductoMenu {
  private Long id;
  private Long restauranteId;
  private String nombre;
  private BigDecimal precio;
  private Boolean disponible;
}
