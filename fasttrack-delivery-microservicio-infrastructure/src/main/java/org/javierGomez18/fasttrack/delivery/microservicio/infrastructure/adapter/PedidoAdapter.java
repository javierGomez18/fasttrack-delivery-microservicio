package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.LineaPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.Pedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.pedido.PedidoRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.LineaPedidoEntityMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.PedidoEntityMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.repository.LineaPedidoR2dbRepository;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.repository.PedidoR2dbRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PedidoAdapter implements PedidoRepositoryPort {

    private final PedidoR2dbRepository pedidoR2dbRepository;
    private final LineaPedidoR2dbRepository lineaPedidoR2dbRepository;
    private final PedidoEntityMapper pedidoEntityMapper;
    private final LineaPedidoEntityMapper lineaPedidoEntityMapper;

    @Override
    public Mono<Pedido> getPedidoById(Long id) {
        return pedidoR2dbRepository.findById(id)
                .map(pedidoEntityMapper::toDomain)
                .flatMap(this::getLineasByPedido);
    }

    @Override
    public Flux<Pedido> getPedidosByRestauranteId(Long id) {
        return pedidoR2dbRepository.findByRestauranteId(id)
                .map(pedidoEntityMapper::toDomain)
                .collectList()
                .flatMapMany(this::getLineasByPedidoLst);
    }

    @Override
    public Flux<Pedido> getAllPedidos() {
        return pedidoR2dbRepository.findAll()
                .map(pedidoEntityMapper::toDomain)
                .collectList()
                .flatMapMany(this::getLineasByPedidoLst);
    }

    @Override
    public Flux<Pedido> getPedidosByClienteId(Long id) {
        return pedidoR2dbRepository.findByClienteId(id)
                .map(pedidoEntityMapper::toDomain)
                .collectList()
                .flatMapMany(this::getLineasByPedidoLst);
    }

    @Override
    public Mono<Pedido> createPedido(Pedido pedido) {
        return pedidoR2dbRepository.save(pedidoEntityMapper.toEntity(pedido))
                .map(pedidoEntityMapper::toDomain);
    }

    @Override
    public Mono<Pedido> cancelPedido(Pedido pedido) {
        return pedidoR2dbRepository.save(pedidoEntityMapper.toEntity(pedido))
                .map(pedidoSaved -> {
                    Pedido result = pedidoEntityMapper.toDomain(pedidoSaved);
                    result.setLineas(pedido.getLineas());
                    return result;
                });
    }

    private Mono<Pedido> getLineasByPedido(Pedido pedido) {
        return lineaPedidoR2dbRepository.findByPedidoId(pedido.getId())
                .collectList()
                .map(lineas -> {
                    pedido.setLineas(lineaPedidoEntityMapper.toDomainList(lineas));
                    return pedido;
                });
    }

    private Flux<Pedido> getLineasByPedidoLst(List<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            return Flux.empty();
        }
        return lineaPedidoR2dbRepository.findByPedidoIdIn(pedidos.stream()
                        .map(Pedido::getId).toList())
                .map(lineaPedidoEntityMapper::toDomain)
                .collect(Collectors.groupingBy(LineaPedido::getPedidoId))
                .flatMapMany(lineasByPedido -> {
                    pedidos.forEach(pedido ->
                            pedido.setLineas(lineasByPedido.getOrDefault(pedido.getId(), List.of())));
                    return Flux.fromIterable(pedidos);
                });
    }
}
