package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.QuantidadeValorDTO;

public interface CalcularValoresItensProdutoPort {
    QuantidadeValorDTO calcular(Long idProduto);
}
