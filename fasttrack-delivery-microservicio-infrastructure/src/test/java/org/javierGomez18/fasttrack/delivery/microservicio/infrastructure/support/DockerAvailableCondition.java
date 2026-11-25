package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.support;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Salta los tests de integración si no hay Docker. Se registra con @ExtendWith en la clase base,
 * que SÍ se hereda; un @EnabledIf puesto en la clase base no sirve, porque JUnit solo lo busca en
 * la propia clase de test y no en sus superclases.
 */
public class DockerAvailableCondition implements ExecutionCondition {

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    return AbstractPostgresIntegrationTest.dockerAvailable()
        ? ConditionEvaluationResult.enabled("Docker disponible")
        : ConditionEvaluationResult.disabled(
            "Docker no está disponible: se omiten los tests de integración");
  }
}
