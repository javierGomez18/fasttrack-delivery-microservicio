package org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.productomenu;

import lombok.Getter;

@Getter
public class ProductoMenuNotFoundException extends RuntimeException {
    public enum Tipo {
        PRODUCTO_MENU_NOT_FOUND,
    }
    private final Tipo tipo;

    public ProductoMenuNotFoundException(Long productoId) {
        super("No se encontró el producto de menú con id " + productoId);
        this.tipo = Tipo.PRODUCTO_MENU_NOT_FOUND;
    }
}
