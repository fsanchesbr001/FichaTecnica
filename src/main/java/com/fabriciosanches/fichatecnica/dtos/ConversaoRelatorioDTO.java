package com.fabriciosanches.fichatecnica.dtos;

import java.math.BigDecimal;

/**
 * DTO de projeção usado exclusivamente para geração de relatórios PDF de Conversão.
 * Os nomes das Unidades de Medida são resolvidos via JPQL JOIN, sem chamadas extras ao serviço.
 */
public record ConversaoRelatorioDTO(
        String unidadeDe,
        String unidadePara,
        String operacao,
        BigDecimal valor
) {}

