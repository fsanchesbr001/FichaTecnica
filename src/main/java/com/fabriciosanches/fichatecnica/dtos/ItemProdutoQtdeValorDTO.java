package com.fabriciosanches.fichatecnica.dtos;

import lombok.Getter;

import java.math.BigDecimal;

public record ItemProdutoQtdeValorDTO(Long cdProduto, Integer quantidade, BigDecimal valor) {
}
