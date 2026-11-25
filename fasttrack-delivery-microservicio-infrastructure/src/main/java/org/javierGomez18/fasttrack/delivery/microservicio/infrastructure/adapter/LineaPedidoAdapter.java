package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.model.LineaPedido;
import org.javierGomez18.fasttrack.delivery.microservicio.domain.port.out.lineaspedido.LineasPedidoRepositoryPort;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper.LineaPedidoEntityMapper;
import org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.persistence.repository.LineaPedidoR2dbRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LineaPedidoAdapter implements LineasPedidoRepositoryPort {

    private final LineaPedidoR2dbRepository lineaPedidoR2dbRepository;
    private final LineaPedidoEntityMapper lineaPedidoEntityMapper;

    @Override
    public Flux<LineaPedido> getLineasPedidosByPedidoId(Long id) {
        return lineaPedidoR2dbRepository.findByPedidoId(id)
                .map(lineaPedidoEntityMapper::toDomain);
    }

    @Override
    public Flux<LineaPedido> createLineaPedidoLst(List<LineaPedido> lineasPedidoLst) {
        return lineaPedidoR2dbRepository.saveAll(lineaPedidoEntityMapper.toEntityList(lineasPedidoLst))
                .map(lineaPedidoEntityMapper::toDomain);
    }
}
