package com.fabriciosanches.fichatecnica.dtos;


import java.math.BigDecimal;

public record ItemProdutoDTO(Long cdItem, Long cdProduto, Double qtdItem,
              Long cdUnidadeMedida, BigDecimal vlrItem) {


}
