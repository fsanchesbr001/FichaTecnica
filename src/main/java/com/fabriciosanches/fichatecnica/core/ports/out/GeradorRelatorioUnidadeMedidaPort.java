package com.fabriciosanches.fichatecnica.core.ports.out;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;

import java.util.List;

public interface GeradorRelatorioUnidadeMedidaPort {
    byte[] gerarRelatorioLista(List<UnidadeMedida> unidades);

    byte[] gerarRelatorioDetalhe(UnidadeMedida unidade);
}
