package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;

public interface AtualizarUnidadeMedidaPort {
    UnidadeMedida atualizar(Long id, String nome, String sigla);
}
