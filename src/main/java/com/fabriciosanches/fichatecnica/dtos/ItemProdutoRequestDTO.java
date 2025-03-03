package com.fabriciosanches.fichatecnica.dtos;

import java.math.BigDecimal;

public record ItemProdutoRequestDTO(Long cdProduto, Long cdItem,
                                    Integer quantidade, Long cdUnidadePara,
                                    BigDecimal valor) {
}
