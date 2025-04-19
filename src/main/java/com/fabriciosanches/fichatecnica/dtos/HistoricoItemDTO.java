package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.domains.HistoricoItem;
import com.fabriciosanches.fichatecnica.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HistoricoItemDTO(Long codigo, Long idItem,
                               @JsonSerialize(using = BigDecimalCurrencySerializer.class) BigDecimal valor, LocalDate dataInicio) {
    public HistoricoItemDTO(HistoricoItem historicoItem) {
        this(historicoItem.getCodigo(), historicoItem.getCdItem(), historicoItem.getValor(),
                historicoItem.getDataInicio());

    }

    public static List<HistoricoItemDTO> from(List<HistoricoItem> all) {
        return all.stream().map(HistoricoItemDTO::new).toList();
    }
}
