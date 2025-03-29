package com.fabriciosanches.fichatecnica.dtos;


import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.domains.UnidadeMedida;

import java.math.BigDecimal;
import java.util.List;

public record ItemDTO(Long codigo, String nome, UnidadeMedida unidadeMedida,
                      BigDecimal valor) {
    public ItemDTO(Item item) {
        this(item.getCodigo(),item.getNome(), item.getUnidadeMedida(), item.getValor());
    }
    public static List<ItemDTO> from(List<Item> lista) {
        return lista.stream().map(ItemDTO::new).toList();
    }

}
