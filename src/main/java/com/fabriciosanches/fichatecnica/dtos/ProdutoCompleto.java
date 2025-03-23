package com.fabriciosanches.fichatecnica.dtos;

import java.math.BigDecimal;

public record ProdutoCompleto(String nomeProduto, String nomeItem, Integer qtdeItem,
                              Long cdUnidade, BigDecimal valorItem) {

}
