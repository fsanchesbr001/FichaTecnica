package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioListaUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.GeradorRelatorioUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class GerarRelatorioListaUnidadeMedidaUseCase implements GerarRelatorioListaUnidadeMedidaPort {

    private final UnidadeMedidaRepositoryPort repositoryPort;
    private final GeradorRelatorioUnidadeMedidaPort reportPort;

    public GerarRelatorioListaUnidadeMedidaUseCase(UnidadeMedidaRepositoryPort repositoryPort,
                                                    GeradorRelatorioUnidadeMedidaPort reportPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
        this.reportPort = Objects.requireNonNull(reportPort, "Report port não pode ser nulo");
    }

    @Override
    public byte[] executar() {
        List<UnidadeMedida> unidades = repositoryPort.buscarTodos().stream()
                .sorted(Comparator.comparing(UnidadeMedida::getNome))
                .toList();

        if (unidades.isEmpty()) {
            throw new NoSuchElementException("Nenhuma unidade de medida encontrada para gerar relatório");
        }

        return reportPort.gerarRelatorioLista(unidades);
    }
}
