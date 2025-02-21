package com.fabriciosanches.fichatecnica.domain.itens;


import com.fabriciosanches.fichatecnica.domain.medidas.UnidadeMedida;

import java.math.BigDecimal;
import java.util.List;

public record DadosItem(Long codigo, String nome, UnidadeMedida unidadeMedida, BigDecimal valor) {
    public DadosItem(Item item) {
        this(item.getCodigo(), item.getNome(), item.getUnidadeMedida(), item.getValor());
    }
    public static List<DadosItem> from(List<Item> lista) {
        return lista.stream().map(DadosItem::new).toList();
    }
}
