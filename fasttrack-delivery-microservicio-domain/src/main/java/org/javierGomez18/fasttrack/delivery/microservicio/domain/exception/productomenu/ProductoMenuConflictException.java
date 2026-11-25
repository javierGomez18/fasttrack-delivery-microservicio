package org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.productomenu;

import lombok.Getter;

@Getter
public class ProductoMenuConflictException extends RuntimeException {

  public enum Tipo {
    PRODUCTO_MENU_DUPLICADO,
  }

  private final Tipo tipo;

  public ProductoMenuConflictException(String nombre) {
    super("El producto con nombre " + nombre + " ya existe en el menú.");
    this.tipo = Tipo.PRODUCTO_MENU_DUPLICADO;
  }
}
