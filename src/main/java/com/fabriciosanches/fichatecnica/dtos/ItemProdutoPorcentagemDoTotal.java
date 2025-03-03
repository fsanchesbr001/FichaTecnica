package com.fabriciosanches.fichatecnica.dtos;

import java.math.BigDecimal;

public record ItemProdutoPorcentagemDoTotal(Long produtoId, Long itemId,
                                            String itemName, BigDecimal porcentagem) {
}
