package org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante;

import lombok.Getter;

@Getter
public class RestauranteNotFoundException extends RuntimeException {
    public enum Tipo {
        RESTAURANTE_NOT_FOUND,
    }
    private final Tipo tipo;

    public RestauranteNotFoundException(Long id) {
        super("No se encontró el restaurante con id " + id);
        this.tipo = Tipo.RESTAURANTE_NOT_FOUND;
    }
}
