package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;

public record ProdutoCompletoDTO(String nomeProduto, String nomeItem, Long idItem, Double qtdeItem,
                                 Long cdUnidade,
                                 @JsonSerialize(using = BigDecimalCurrencySerializer.class) BigDecimal valorItem) {

}
