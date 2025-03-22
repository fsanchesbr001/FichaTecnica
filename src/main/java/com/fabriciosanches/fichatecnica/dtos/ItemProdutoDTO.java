package com.fabriciosanches.fichatecnica.dtos;


import java.math.BigDecimal;

public record ItemProdutoDTO(Long cdItem, Long cdProduto, Integer qtdItem,
              Long cdUnidadeMedida, BigDecimal vlrItem) {


}
