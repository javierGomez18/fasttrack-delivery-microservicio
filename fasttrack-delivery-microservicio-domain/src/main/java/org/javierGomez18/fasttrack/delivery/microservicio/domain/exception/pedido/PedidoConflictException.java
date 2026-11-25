package org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido;

import lombok.Getter;

@Getter
public class PedidoConflictException extends RuntimeException {
    public enum Tipo {
        PEDIDO_NOT_CANCELABLE,
    }
    private final Tipo tipo;

    public PedidoConflictException(Long id) {
        super("El pedido con id " + id + " no se puede cancelar");
        this.tipo = Tipo.PEDIDO_NOT_CANCELABLE;
    }
}
