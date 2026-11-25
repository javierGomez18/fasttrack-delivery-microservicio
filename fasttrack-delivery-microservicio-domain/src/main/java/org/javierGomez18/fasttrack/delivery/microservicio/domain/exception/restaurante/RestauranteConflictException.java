package org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante;

import lombok.Getter;

@Getter
public class RestauranteConflictException extends RuntimeException {
    public enum Tipo {
        RESTAURANTE_DUPLICADO,
    }
    private final Tipo tipo;

    public RestauranteConflictException(String nombre) {
        super("El restaurante con nombre " + nombre + " ya existe");
        this.tipo = Tipo.RESTAURANTE_DUPLICADO;
    }
}
