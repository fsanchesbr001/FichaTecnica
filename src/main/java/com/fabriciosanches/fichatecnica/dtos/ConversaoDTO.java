package com.fabriciosanches.fichatecnica.dtos;


import com.fabriciosanches.fichatecnica.domains.Conversao;
import com.fabriciosanches.fichatecnica.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.util.List;

public record ConversaoDTO(
        @JsonProperty("codigo") Long codigo,
        @JsonProperty("unidadeDe") Long unidadeDe,
        @JsonProperty("unidadePara") Long unidadePara,
        @JsonProperty("operacao") String operacao,
        @JsonProperty("valor") @JsonSerialize(using = BigDecimalCurrencySerializer.class) BigDecimal valor) {

    public ConversaoDTO(Conversao conversao) {
        this(conversao.getCodigo(), conversao.getUnidadeDe(), conversao.getUnidadePara(), conversao.getOperacao(),
                conversao.getValor());
    }

    public static List<ConversaoDTO> from(List<Conversao> lista) {
        return lista.stream().map(ConversaoDTO::new).toList();
    }
}
