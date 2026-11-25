package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("PEDIDO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PedidoEntity {

  @Id private Long id;

  @Column("RESTAURANTE_ID")
  private Long restauranteId;

  @Column("CLIENTE_ID")
  private Long clienteId;

  @Column("ESTADO")
  private String estado;

  @Column("FECHA_CREACION")
  private LocalDateTime fechaCreacion;

  @Column("TOTAL")
  private BigDecimal total;

  @Column("TIEMPO_ESTIMADO_MINUTOS")
  private Integer tiempoEstimadoMinutos;
}
