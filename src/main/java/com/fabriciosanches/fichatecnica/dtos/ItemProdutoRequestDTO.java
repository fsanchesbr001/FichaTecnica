package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.domains.Produto;

import java.math.BigDecimal;

public record ItemProdutoRequestDTO(Long cdProduto, Long cdItem,
                                    Integer quantidade, Long cdUnidadePara,
                                    BigDecimal valor, Produto produto) {
}
