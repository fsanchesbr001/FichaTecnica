package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.QuantidadeValorDTO;

public interface CalcularValoresItensProdutoPort {
    QuantidadeValorDTO calcular(Long idProduto);
}

