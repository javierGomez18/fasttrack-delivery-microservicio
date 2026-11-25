package org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido;

import lombok.Getter;

@Getter
public class PedidoUnprocessableEntity extends RuntimeException {

    public enum Tipo {
        PEDIDO_SIN_LINEAS,
    }

    private final Tipo tipo;

    public PedidoUnprocessableEntity() {
        super("El pedido no puede procesarse porque no tiene líneas de pedido.");
        this.tipo = Tipo.PEDIDO_SIN_LINEAS;
    }
}
