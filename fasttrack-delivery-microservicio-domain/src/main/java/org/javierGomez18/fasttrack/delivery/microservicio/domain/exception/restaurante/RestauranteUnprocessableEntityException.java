package org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante;

import lombok.Getter;

@Getter
public class RestauranteUnprocessableEntityException extends RuntimeException {

    public enum Tipo {
        RESTAURANTE_INACTIVO,
    }

    private final Tipo tipo;

    public RestauranteUnprocessableEntityException() {
        super("El restaurante esta inactivo.");
        this.tipo = Tipo.RESTAURANTE_INACTIVO;
    }
}
