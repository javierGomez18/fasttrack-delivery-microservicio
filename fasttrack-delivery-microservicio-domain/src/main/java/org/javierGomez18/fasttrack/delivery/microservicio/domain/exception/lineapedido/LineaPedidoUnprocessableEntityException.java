package org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.lineapedido;

import lombok.Getter;

@Getter
public class LineaPedidoUnprocessableEntityException extends RuntimeException {

    public enum Tipo {
        PRODUCTO_NO_DISPONIBLE,
        PRODUCTO_NO_PERTENECE_A_RESTAURANTE,
    }
    private final Tipo tipo;

    public LineaPedidoUnprocessableEntityException() {
        super("El producto no esta disponible en este momento.");
        this.tipo=Tipo.PRODUCTO_NO_DISPONIBLE;
    }

    public LineaPedidoUnprocessableEntityException(Long restauranteId) {
        super("El producto no pertenece al restaurante con el id "+restauranteId+".");
        this.tipo=Tipo.PRODUCTO_NO_PERTENECE_A_RESTAURANTE;
    }
}
