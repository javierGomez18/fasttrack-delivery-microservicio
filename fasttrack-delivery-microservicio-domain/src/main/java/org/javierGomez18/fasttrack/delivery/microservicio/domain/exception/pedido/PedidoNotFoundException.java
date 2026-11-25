package org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido;

import lombok.Getter;

@Getter
public class PedidoNotFoundException extends RuntimeException {
    public enum Tipo {
        PEDIDO_NOT_FOUND,
    }
    private final Tipo tipo;

    public PedidoNotFoundException(Long id) {
        super("No se encontró el pedido con id " + id);
        this.tipo = Tipo.PEDIDO_NOT_FOUND;
    }
}
