package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;

public interface GerarGraficoPizzaProdutoPort {
    GraficoPizzaDTO gerar(Long idProduto);
}
