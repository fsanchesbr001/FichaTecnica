package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.HistoricoItem;
import com.fabriciosanches.fichatecnica.core.ports.in.RegistrarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.HistoricoItemRepositoryPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class RegistrarHistoricoItemUseCase implements RegistrarHistoricoItemPort {
    private final HistoricoItemRepositoryPort historicoItemRepositoryPort;

    public RegistrarHistoricoItemUseCase(HistoricoItemRepositoryPort historicoItemRepositoryPort) {
        this.historicoItemRepositoryPort = Objects.requireNonNull(historicoItemRepositoryPort, "HistoricoItem repository port não pode ser nulo");
    }

    @Override
    public HistoricoItem registrar(Long codigoItem, BigDecimal valor, LocalDate dataInicio) {
        if (codigoItem == null) {
            throw new IllegalArgumentException("Código do item não pode ser nulo");
        }
        if (valor == null) {
            throw new IllegalArgumentException("Valor não pode ser nulo");
        }

        LocalDate data = dataInicio != null ? dataInicio : LocalDate.now();
        return historicoItemRepositoryPort.salvar(new HistoricoItem(null, codigoItem, valor, data));
    }
}

