package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.config;

import org.javierGomez18.fasttrack.delivery.microservicio.application.cancel.pedido.CancelPedidoService;
import org.javierGomez18.fasttrack.delivery.microservicio.application.create.pedido.CreatePedidoService;
import org.javierGomez18.fasttrack.delivery.microservicio.application.create.restaurante.CreateRestauranteService;
import org.javierGomez18.fasttrack.delivery.microservicio.application.find.pedido.FindPedidoService;
import org.javierGomez18.fasttrack.delivery.microservicio.application.find.restaurante.FindRestauranteService;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.CancelPedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.CreatePedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.pedido.FindPedidoUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.restaurante.CreateRestauranteUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.in.restaurante.FindRestauranteUseCase;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.lineaspedido.LineasPedidoRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.pedido.PedidoRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.productomenu.ProductoMenuRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.restaurante.RestauranteRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.reactive.TransactionalOperator;

/**
 * Registra los servicios de {@code application} como beans. Esos servicios no llevan @Service para
 * que el módulo application no dependa de Spring; es infrastructure quien los cablea con los
 * adapters (que implementan los puertos de salida) y con el TransactionalOperator que autoconfigura
 * Spring Boot a partir de R2DBC.
 */
@Configuration
public class UseCaseConfiguration {

    @Bean
    public FindPedidoUseCase findPedidoUseCase(PedidoRepositoryPort pedidoRepositoryPort) {
        return new FindPedidoService(pedidoRepositoryPort);
    }

    @Bean
    public CancelPedidoUseCase cancelPedidoUseCase(PedidoRepositoryPort pedidoRepositoryPort) {
        return new CancelPedidoService(pedidoRepositoryPort);
    }

    @Bean
    public CreatePedidoUseCase createPedidoUseCase(
            PedidoRepositoryPort pedidoRepositoryPort,
            LineasPedidoRepositoryPort lineasPedidoRepositoryPort,
            RestauranteRepositoryPort restauranteRepositoryPort,
            ProductoMenuRepositoryPort productoMenuRepositoryPort,
            TransactionalOperator transactionalOperator) {
        return new CreatePedidoService(
                pedidoRepositoryPort,
                lineasPedidoRepositoryPort,
                restauranteRepositoryPort,
                productoMenuRepositoryPort,
                transactionalOperator);
    }

    @Bean
    public FindRestauranteUseCase findRestauranteUseCase(
            RestauranteRepositoryPort restauranteRepositoryPort,
            ProductoMenuRepositoryPort productoMenuRepositoryPort) {
        return new FindRestauranteService(restauranteRepositoryPort, productoMenuRepositoryPort);
    }

    @Bean
    public CreateRestauranteUseCase createRestauranteUseCase(
            RestauranteRepositoryPort restauranteRepositoryPort,
            ProductoMenuRepositoryPort productoMenuRepositoryPort,
            TransactionalOperator transactionalOperator) {
        return new CreateRestauranteService(
                restauranteRepositoryPort, productoMenuRepositoryPort, transactionalOperator);
    }
}
