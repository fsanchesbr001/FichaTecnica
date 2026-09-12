package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;


import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;

public record ItemProdutoDTO(Long cdItem, Long cdProduto, Double qtdItem,
              Long cdUnidadeMedida, @JsonSerialize(using = BigDecimalCurrencySerializer.class) BigDecimal vlrItem) {}

