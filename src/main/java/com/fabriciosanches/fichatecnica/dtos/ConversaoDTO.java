package com.fabriciosanches.fichatecnica.dtos;


import com.fabriciosanches.fichatecnica.domains.Conversao;
import com.fabriciosanches.fichatecnica.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.util.List;

public record ConversaoDTO(Long codigo, Long unidadeDe, Long unidadePara, String operacao,
                           @JsonSerialize(using = BigDecimalCurrencySerializer.class) BigDecimal valor) {
    public ConversaoDTO(Conversao conversao) {
        this(conversao.getCodigo(), conversao.getUnidadeDe(), conversao.getUnidadePara(), conversao.getOperacao(),
                conversao.getValor());
    }

    public static List<ConversaoDTO> from(List<Conversao> lista) {
        return lista.stream().map(ConversaoDTO::new).toList();
    }
}
