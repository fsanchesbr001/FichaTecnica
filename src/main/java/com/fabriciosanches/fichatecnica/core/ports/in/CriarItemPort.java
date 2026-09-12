package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;

import java.math.BigDecimal;

public interface CriarItemPort {
    Item criar(String nome, UnidadeMedida unidadeMedida, BigDecimal valor);
}


