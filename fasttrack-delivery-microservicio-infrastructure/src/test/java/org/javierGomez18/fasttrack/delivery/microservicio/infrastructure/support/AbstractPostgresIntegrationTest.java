package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.support;

import java.util.UUID;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.productomenu.ProductoMenuRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.restaurante.RestauranteRepositoryPort;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Base de los tests de integración: arranca la aplicación completa (servidor Netty real en un
 * puerto aleatorio) contra un PostgreSQL real levantado con Docker.
 *
 * <p>Al arrancar, Spring ejecuta schema.sql y data.sql sobre ese Postgres, igual que en local. El
 * contenedor es un singleton compartido por todas las clases de test: Spring cachea el contexto
 * entre clases y ese contexto guarda la URL (con el puerto) del primer contenedor, así que si cada
 * clase levantara el suyo, las siguientes apuntarían a un puerto muerto. Se destruye al terminar la
 * JVM (Ryuk).
 *
 * <p>Si Docker no está disponible los tests se saltan en vez de fallar.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(DockerAvailableCondition.class)
public abstract class AbstractPostgresIntegrationTest {

  private static final String USER = "fasttrack";
  private static final String PASSWORD = "fasttrack";
  private static final String DB = "fasttrack_test";

  private static final GenericContainer<?> POSTGRES =
      new GenericContainer<>("postgres:16-alpine")
          .withEnv("POSTGRES_USER", USER)
          .withEnv("POSTGRES_PASSWORD", PASSWORD)
          .withEnv("POSTGRES_DB", DB)
          .withExposedPorts(5432)
          // Postgres arranca dos veces (init y definitivo): este mensaje sale la segunda vez
          .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2));

  static {
    if (dockerAvailable()) {
      POSTGRES.start();
    }
  }

  static boolean dockerAvailable() {
    try {
      return DockerClientFactory.instance().isDockerAvailable();
    } catch (Throwable e) {
      return false;
    }
  }

  @Autowired private RestauranteRepositoryPort restauranteRepositoryPort;
  @Autowired private ProductoMenuRepositoryPort productoMenuRepositoryPort;

  /** Id de un restaurante de data.sql (activo o no) buscándolo por nombre. */
  protected Long restauranteId(String nombre) {
    return restauranteRepositoryPort
        .getAllRestaurantes()
        .filter(r -> r.getNombre().equals(nombre))
        .map(Restaurante::getId)
        .blockFirst();
  }

  /** Id de un producto de data.sql buscándolo por restaurante y nombre. */
  protected Long productoId(Long restauranteId, String nombre) {
    return productoMenuRepositoryPort
        .getByRestauranteId(restauranteId)
        .filter(p -> p.getNombre().equals(nombre))
        .map(ProductoMenu::getId)
        .blockFirst();
  }

  /** Nombre único para que los tests que insertan no choquen con los UNIQUE ni entre sí. */
  protected static String unico(String prefijo) {
    return prefijo + "-" + UUID.randomUUID().toString().substring(0, 8);
  }

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.r2dbc.url",
        () ->
            "r2dbc:postgresql://%s:%d/%s"
                .formatted(POSTGRES.getHost(), POSTGRES.getMappedPort(5432), DB));
    registry.add("spring.r2dbc.username", () -> USER);
    registry.add("spring.r2dbc.password", () -> PASSWORD);
  }
}
