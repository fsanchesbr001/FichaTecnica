package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.domains.HistoricoItem;
import com.fabriciosanches.fichatecnica.domains.Item;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HistoricoItemDTO(Long codigo, Item item, BigDecimal valor, LocalDate dataInicio) {
    public HistoricoItemDTO(HistoricoItem historicoItem) {
        this(historicoItem.getCodigo(), historicoItem.getItem(), historicoItem.getValor(),
                historicoItem.getDataInicio());

    }

    public static List<HistoricoItemDTO> from(List<HistoricoItem> all) {
        return all.stream().map(com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO::new).toList();
    }
}
