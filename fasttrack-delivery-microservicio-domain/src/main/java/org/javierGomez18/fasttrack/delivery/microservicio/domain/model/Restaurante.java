package org.javierGomez18.fasttrack.delivery.microservicio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Restaurante {
  private Long id;
  private String nombre;
  private CategoriaRestaurante categoria;
  private Boolean activo;
  private List<ProductoMenu> productoMenuLst;
  private List<Pedido> pedidosLst;
}
