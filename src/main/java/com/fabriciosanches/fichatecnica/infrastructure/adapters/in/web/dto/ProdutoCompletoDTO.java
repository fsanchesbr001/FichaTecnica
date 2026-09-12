package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;

public record ProdutoCompletoDTO(String nomeProduto, String nomeItem, Long idItem, Double qtdeItem,
                                 Long cdUnidade,
                                 @JsonSerialize(using = BigDecimalCurrencySerializer.class) BigDecimal valorItem) {

}

