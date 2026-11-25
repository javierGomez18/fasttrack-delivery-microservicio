package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** metadata se guarda como JSON serializado (columna JSONB en Postgres), ver MetadataMapper. */
@Table("EVENTO_SEGUIMIENTO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventoSeguimientoEntity {

  @Id private Long id;

  @Column("PEDIDO_ID")
  private Long pedidoId;

  @Column("TIPO")
  private String tipo;

  @Column("TIMESTAMP")
  private LocalDateTime timestamp;

  @Column("METADATA")
  private String metadata;
}
