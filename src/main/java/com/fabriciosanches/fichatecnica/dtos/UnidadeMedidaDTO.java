package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.domains.UnidadeMedida;

import java.util.List;

public record UnidadeMedidaDTO(Long codigo, String nome, String sigla) {
    public UnidadeMedidaDTO(UnidadeMedida unidadeMedida) {
        this(unidadeMedida.getCodigo(), unidadeMedida.getNome(), unidadeMedida.getSigla());
    }

    public static UnidadeMedida toEntity(UnidadeMedidaDTO dto) {
        return new UnidadeMedida(dto.codigo(), dto.nome(), dto.sigla());
    }

    public static List<UnidadeMedidaDTO> from(List<UnidadeMedida> lista) {
        return lista.stream().map(UnidadeMedidaDTO::new).toList();
    }
}
