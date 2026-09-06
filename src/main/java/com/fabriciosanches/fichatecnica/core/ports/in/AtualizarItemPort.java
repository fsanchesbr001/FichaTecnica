package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UnidadeMedidaEntity;

import java.math.BigDecimal;

public interface AtualizarItemPort {
    Item atualizar(Long id, String nome, UnidadeMedidaEntity unidadeMedida, BigDecimal valor);
}

