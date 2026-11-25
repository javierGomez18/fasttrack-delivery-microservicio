package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;

/** Comprueba que schema.sql y data.sql se aplican correctamente sobre un Postgres real. */
class SchemaIntegrationTest extends AbstractPostgresIntegrationTest {

  @Autowired DatabaseClient db;

  private Long count(String table) {
    return db.sql("SELECT count(*) FROM " + table)
        .map(row -> row.get(0, Long.class))
        .one()
        .block();
  }

  @Test
  void elEsquemaYLosDatosDeEjemploSeCargan() {
    assertThat(count("restaurante")).isGreaterThanOrEqualTo(5);
    assertThat(count("producto_menu")).isGreaterThanOrEqualTo(19);
    assertThat(count("pedido")).isGreaterThanOrEqualTo(4);
    assertThat(count("linea_pedido")).isGreaterThanOrEqualTo(7);
    assertThat(count("evento_seguimiento")).isGreaterThanOrEqualTo(12);
  }
}
