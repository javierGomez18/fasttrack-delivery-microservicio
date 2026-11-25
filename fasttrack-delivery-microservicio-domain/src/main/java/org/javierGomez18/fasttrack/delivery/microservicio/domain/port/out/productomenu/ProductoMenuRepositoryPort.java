package org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.productomenu;


import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Restaurante;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ProductoMenuRepositoryPort {

    Flux<ProductoMenu> createProductoMenuLst(List<ProductoMenu> productoMenuLst);
    Flux<ProductoMenu> getByRestauranteId(Long restauranteId);
    Flux<ProductoMenu> getByIds(List<Long> productoMenuIds);
}
