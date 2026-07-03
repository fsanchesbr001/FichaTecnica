package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;

import java.util.List;

public interface BuscarUnidadeMedidaPort {
    List<UnidadeMedida> buscarTodos();

    UnidadeMedida buscarPorId(Long id);
}
