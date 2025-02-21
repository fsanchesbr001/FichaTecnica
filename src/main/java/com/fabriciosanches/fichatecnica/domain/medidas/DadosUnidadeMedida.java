package com.fabriciosanches.fichatecnica.domain.medidas;

import java.util.List;

public record DadosUnidadeMedida(Long codigo, String nome, String sigla) {
    public DadosUnidadeMedida(UnidadeMedida unidadeMedida) {
        this(unidadeMedida.getCodigo(), unidadeMedida.getNome(), unidadeMedida.getSigla());
    }

    public static List<DadosUnidadeMedida> from(List<UnidadeMedida> lista) {
        return lista.stream().map(DadosUnidadeMedida::new).toList();
    }
}
