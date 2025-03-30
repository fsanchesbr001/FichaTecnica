package com.fabriciosanches.fichatecnica.dtos;

import java.math.BigDecimal;

public record ProdutoCompletoDTO(String nomeProduto, String nomeItem, Double qtdeItem,
                                 Long cdUnidade,
                                 BigDecimal valorItem) {

}
