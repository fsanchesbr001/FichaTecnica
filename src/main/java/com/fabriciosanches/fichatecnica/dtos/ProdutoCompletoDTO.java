package com.fabriciosanches.fichatecnica.dtos;

import java.math.BigDecimal;

public record ProdutoCompletoDTO(String nomeProduto, String nomeItem, Integer qtdeItem,
                                 Long cdUnidade,
                                 BigDecimal valorItem) {

}
