package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BuscarUnidadeMedidaUseCase implements BuscarUnidadeMedidaPort {
    private final UnidadeMedidaRepositoryPort repositoryPort;

    public BuscarUnidadeMedidaUseCase(UnidadeMedidaRepositoryPort repositoryPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
    }

    @Override
    public List<UnidadeMedida> buscarTodos() {
        return repositoryPort.buscarTodos().stream()
                .sorted(Comparator.comparing(UnidadeMedida::getNome))
                .toList();
    }

    @Override
    public UnidadeMedida buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id não pode ser nulo");
        }

        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Unidade de medida não encontrada"));
    }
}
