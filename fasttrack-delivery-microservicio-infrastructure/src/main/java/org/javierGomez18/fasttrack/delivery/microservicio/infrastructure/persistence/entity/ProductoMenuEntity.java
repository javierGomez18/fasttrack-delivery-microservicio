package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("PRODUCTO_MENU")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductoMenuEntity {

  @Id private Long id;

  @Column("RESTAURANTE_ID")
  private Long restauranteId;

  @Column("NOMBRE")
  private String nombre;

  @Column("PRECIO")
  private BigDecimal precio;

  @Column("DISPONIBLE")
  private Boolean disponible;
}
