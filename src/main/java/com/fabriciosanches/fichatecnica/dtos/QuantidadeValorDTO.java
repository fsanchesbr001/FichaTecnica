package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;

public record QuantidadeValorDTO(int quantidadeTotal,
                                 @JsonSerialize(using = BigDecimalCurrencySerializer.class) BigDecimal valorTotal) {
}
