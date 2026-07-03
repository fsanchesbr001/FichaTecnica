package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;

public interface CriarUnidadeMedidaPort {
    UnidadeMedida criar(String nome, String sigla);
}
