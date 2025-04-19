package com.fabriciosanches.fichatecnica.dtos;


import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.domains.UnidadeMedida;
import com.fabriciosanches.fichatecnica.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.util.List;

public record ItemDTO(Long codigo, String nome, UnidadeMedida unidadeMedida,
                      @JsonSerialize(using = BigDecimalCurrencySerializer.class) BigDecimal valor) {
    public ItemDTO(Item item) {
        this(item.getCodigo(),item.getNome(), item.getUnidadeMedida(), item.getValor());
    }

    public static List<ItemDTO> from(List<Item> lista) {
        return lista.stream().map(ItemDTO::new).toList();
    }

}
