package org.javierGomez18.fasttrack.delivery.microservicio.application.create.pedido;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.lineapedido.LineaPedidoUnprocessableEntityException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.pedido.PedidoUnprocessableEntity;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.productomenu.ProductoMenuNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteNotFoundException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.exception.restaurante.RestauranteUnprocessableEntityException;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.LineaPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.ProductoMenu;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.CreatePedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.lineaspedido.LineasPedidoRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.pedido.PedidoRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.productomenu.ProductoMenuRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.restaurante.RestauranteRepositoryPort;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CreatePedidoService implements CreatePedidoUseCase {

    private final PedidoRepositoryPort pedidoRepositoryPort;
    private final LineasPedidoRepositoryPort lineasPedidoRepositoryPort;
    private final RestauranteRepositoryPort restauranteRepositoryPort;
    private final ProductoMenuRepositoryPort productoMenuRepositoryPort;
    private final TransactionalOperator transactionalOperator;

    private static final int TIEMPO_ESTIMADO_POR_DEFECTO = 30;

    @Override
    public Mono<Pedido> createPedido(Pedido pedido) {
        if (pedido.getLineas() == null || pedido.getLineas().isEmpty()) {
            return Mono.error(new PedidoUnprocessableEntity());
        }
        return restauranteRepositoryPort.getRestauranteById(pedido.getRestauranteId())
                .switchIfEmpty(Mono.error(new RestauranteNotFoundException(pedido.getRestauranteId())))
                .filter(restaurante -> Boolean.TRUE.equals(restaurante.getActivo()))
                .switchIfEmpty(Mono.error(new RestauranteUnprocessableEntityException()))
                .flatMap(restaurante -> loadProductosMenu(pedido))
                .map(productos -> preparePedido(pedido, productos))
                .flatMap(this::savePedido);
    }

    private Mono<Pedido> savePedido(Pedido pedido) {
        return pedidoRepositoryPort.createPedido(pedido)
                .flatMap(createdPedido -> {
                    List<LineaPedido> lineasPedidoLst = pedido.getLineas();
                    lineasPedidoLst.forEach(lineaPedido -> lineaPedido.setPedidoId(createdPedido.getId()));
                    return lineasPedidoRepositoryPort.createLineaPedidoLst(lineasPedidoLst)
                            .collectList()
                            .map(savedLineasPedidoLst -> {
                                createdPedido.setLineas(savedLineasPedidoLst);
                                return createdPedido;
                            });
                }).as(transactionalOperator::transactional);
    }

    private Mono<Map<Long, ProductoMenu>> loadProductosMenu(Pedido pedido) {
        return productoMenuRepositoryPort.getByIds(
                        pedido.getLineas().stream()
                                .map(LineaPedido::getProductoId)
                                .distinct()
                                .toList())
                .collectMap(ProductoMenu::getId);
    }

    private Pedido preparePedido(Pedido pedido, Map<Long, ProductoMenu> productoMenuLst) {
        BigDecimal total = BigDecimal.ZERO;
        for (LineaPedido linea : pedido.getLineas()) {
            ProductoMenu producto = productoMenuLst.get(linea.getProductoId());

            if (producto == null) {
                throw new ProductoMenuNotFoundException(linea.getProductoId());
            }
            if (!producto.getRestauranteId().equals(pedido.getRestauranteId())) {
                throw new LineaPedidoUnprocessableEntityException(pedido.getRestauranteId());
            }
            if (!Boolean.TRUE.equals(producto.getDisponible())) {
                throw new LineaPedidoUnprocessableEntityException();
            }

            linea.setPrecioUnitario(producto.getPrecio());
            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(linea.getCantidad())));
        }

        pedido.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setTiempoEstimadoMinutos(TIEMPO_ESTIMADO_POR_DEFECTO);
        return pedido;
    }
}
