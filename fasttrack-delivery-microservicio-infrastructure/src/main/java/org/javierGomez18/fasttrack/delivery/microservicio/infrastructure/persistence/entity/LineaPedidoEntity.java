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

@Table("LINEA_PEDIDO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LineaPedidoEntity {

  @Id private Long id;

  @Column("PEDIDO_ID")
  private Long pedidoId;

  @Column("PRODUCTO_ID")
  private Long productoId;

  @Column("CANTIDAD")
  private Integer cantidad;

  @Column("PRECIO_UNITARIO")
  private BigDecimal precioUnitario;
}
