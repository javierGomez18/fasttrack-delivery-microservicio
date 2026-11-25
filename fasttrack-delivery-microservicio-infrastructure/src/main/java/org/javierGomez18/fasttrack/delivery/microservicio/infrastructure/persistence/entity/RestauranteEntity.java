package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("RESTAURANTE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestauranteEntity {

  @Id private Long id;

  @Column("NOMBRE")
  private String nombre;

  @Column("CATEGORIA")
  private String categoria;

  @Column("ACTIVO")
  private Boolean activo;
}
