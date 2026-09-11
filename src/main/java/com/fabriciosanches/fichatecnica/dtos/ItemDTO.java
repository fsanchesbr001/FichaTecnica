package com.fabriciosanches.fichatecnica.dtos;


import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.ItemEntity;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UnidadeMedidaEntity;
import com.fabriciosanches.fichatecnica.serializers.BigDecimalCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.util.List;

public record ItemDTO(Long codigo, String nome, UnidadeMedidaEntity unidadeMedida,
                      @JsonSerialize(using = BigDecimalCurrencySerializer.class) BigDecimal valor) {
    public ItemDTO(Item item) {
        this(item.getCodigo(), item.getNome(), toEntity(item.getUnidadeMedida()), item.getValor());
    }

    public ItemDTO(ItemEntity item) {
        this(item.getCodigo(), item.getNome(), item.getUnidadeMedida(), item.getValor());
    }

    public static List<ItemDTO> from(List<Item> lista) {
        return lista.stream().map(ItemDTO::new).toList();
    }

    public static List<ItemDTO> fromEntities(List<ItemEntity> lista) {
        return lista.stream().map(ItemDTO::new).toList();
    }

    private static UnidadeMedidaEntity toEntity(UnidadeMedida unidadeMedida) {
        if (unidadeMedida == null) {
            return null;
        }
        return new UnidadeMedidaEntity(unidadeMedida.getCodigo(), unidadeMedida.getNome(), unidadeMedida.getSigla());
    }

}

