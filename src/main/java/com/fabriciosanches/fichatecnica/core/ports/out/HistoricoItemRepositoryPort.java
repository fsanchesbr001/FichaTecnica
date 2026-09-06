package com.fabriciosanches.fichatecnica.core.ports.out;

import com.fabriciosanches.fichatecnica.core.domain.HistoricoItem;

import java.util.List;
import java.util.Optional;

public interface HistoricoItemRepositoryPort {
    HistoricoItem salvar(HistoricoItem historicoItem);

    List<HistoricoItem> buscarTodos();

    Optional<HistoricoItem> buscarPorId(Long id);

    List<HistoricoItem> buscarPorCodigoItem(Long codigoItem);

    List<HistoricoItem> buscarPorCodigoItemOrdenadoPorDataInicio(Long codigoItem);

    void deletarPorCodigoItem(Long codigoItem);
}

