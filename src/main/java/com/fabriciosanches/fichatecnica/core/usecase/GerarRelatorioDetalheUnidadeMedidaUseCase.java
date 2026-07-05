package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioDetalheUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.GeradorRelatorioUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;

import java.util.NoSuchElementException;
import java.util.Objects;

public class GerarRelatorioDetalheUnidadeMedidaUseCase implements GerarRelatorioDetalheUnidadeMedidaPort {

    private final UnidadeMedidaRepositoryPort repositoryPort;
    private final GeradorRelatorioUnidadeMedidaPort reportPort;

    public GerarRelatorioDetalheUnidadeMedidaUseCase(UnidadeMedidaRepositoryPort repositoryPort,
                                                      GeradorRelatorioUnidadeMedidaPort reportPort) {
        this.repositoryPort = Objects.requireNonNull(repositoryPort, "Repository port não pode ser nulo");
        this.reportPort = Objects.requireNonNull(reportPort, "Report port não pode ser nulo");
    }

    @Override
    public byte[] executar(String siglaOuCodigo) {
        String siglaNormalizada = normalizarSigla(siglaOuCodigo);

        UnidadeMedida unidade = repositoryPort.buscarPorSigla(siglaNormalizada)
                .orElseThrow(() -> new NoSuchElementException(
                        "Unidade de medida não encontrada: " + siglaNormalizada));

        return reportPort.gerarRelatorioDetalhe(unidade);
    }

    private String normalizarSigla(String sigla) {
        if (sigla == null || sigla.isBlank()) {
            throw new IllegalArgumentException("Sigla da unidade de medida não pode ser vazia");
        }
        return sigla.trim().toUpperCase();
    }
}
