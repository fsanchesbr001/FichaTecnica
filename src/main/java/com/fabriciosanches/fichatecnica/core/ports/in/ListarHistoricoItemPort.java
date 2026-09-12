package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.HistoricoItem;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.GraficoPrecoItemDTO;

import java.util.List;

public interface ListarHistoricoItemPort {
    List<HistoricoItem> listar();

    HistoricoItem buscarPorId(Long id);

    List<HistoricoItem> listarPorCodigoItem(Long codigoItem);

    List<HistoricoItem> listarPorCodigoItemOrdenadoPorDataInicio(Long codigoItem);

    GraficoPrecoItemDTO gerarGraficoPreco(Long codigoItem);
}


