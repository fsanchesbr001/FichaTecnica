package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.GraficoPizzaDTO;

public interface GerarGraficoPizzaProdutoPort {
    GraficoPizzaDTO gerar(Long idProduto);
}

