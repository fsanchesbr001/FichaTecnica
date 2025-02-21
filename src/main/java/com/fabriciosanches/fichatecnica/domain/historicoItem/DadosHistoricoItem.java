package com.fabriciosanches.fichatecnica.domain.historicoItem;

import com.fabriciosanches.fichatecnica.domain.itens.Item;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DadosHistoricoItem(Long codigo, Item item, BigDecimal valor, LocalDate dataInicio) {
    public DadosHistoricoItem(HistoricoItem historicoItem) {
        this(historicoItem.getCodigo(), historicoItem.getItem(), historicoItem.getValor(),
                historicoItem.getDataInicio());

    }

    public static List<DadosHistoricoItem> from(List<HistoricoItem> all) {
        return all.stream().map(DadosHistoricoItem::new).toList();
    }
}
