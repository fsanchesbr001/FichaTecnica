package com.fabriciosanches.fichatecnica.domain.conversao;


import java.math.BigDecimal;
import java.util.List;

public record DadosConversao(Long codigo,Long unidadeDe, Long unidadePara, String operacao, BigDecimal valor) {
    public DadosConversao(Conversao conversao) {
        this(conversao.getCodigo(), conversao.getUnidadeDe(), conversao.getUnidadePara(), conversao.getOperacao(),
                conversao.getValor());
    }

    public static List<DadosConversao> from(List<Conversao> lista) {
        return lista.stream().map(DadosConversao::new).toList();
    }
}
