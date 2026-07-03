package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UnidadeMedidaEntity;

import java.util.List;

public record UnidadeMedidaDTO(Long codigo, String nome, String sigla) {
    public UnidadeMedidaDTO(UnidadeMedidaEntity unidadeMedida) {
        this(unidadeMedida.getCodigo(), unidadeMedida.getNome(), unidadeMedida.getSigla());
    }

    public static UnidadeMedidaEntity toEntity(UnidadeMedidaDTO dto) {
        return new UnidadeMedidaEntity(dto.codigo(), dto.nome(), dto.sigla());
    }

    public static List<UnidadeMedidaDTO> from(List<UnidadeMedidaEntity> lista) {
        return lista.stream().map(UnidadeMedidaDTO::new).toList();
    }
}
