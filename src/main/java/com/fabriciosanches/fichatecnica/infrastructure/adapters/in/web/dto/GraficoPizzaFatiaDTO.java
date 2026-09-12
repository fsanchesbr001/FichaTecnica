package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

import java.math.BigDecimal;

/**
 * Representa uma fatia do grÃ¡fico de pizza de composiÃ§Ã£o de custo de um Produto.
 *
 * <ul>
 *   <li>{@code nomeItem}            â€“ nome do Item</li>
 *   <li>{@code idItem}              â€“ cÃ³digo do Item (para navegaÃ§Ã£o no frontend)</li>
 *   <li>{@code porcentagem}         â€“ percentual bruto desta fatia sobre o valor total (ex.: 32.5)</li>
 *   <li>{@code porcentagemFormatada}â€“ percentual formatado para exibiÃ§Ã£o (ex.: "32,5%")</li>
 *   <li>{@code valorItem}           â€“ valor monetÃ¡rio formatado deste item (ex.: "R$ 5,04")</li>
 *   <li>{@code valorItemBruto}      â€“ valor numÃ©rico bruto deste item</li>
 *   <li>{@code valorTotal}          â€“ valor total do produto formatado (ex.: "R$ 15,50")</li>
 *   <li>{@code cor}                 â€“ cor hexadecimal atribuÃ­da a esta fatia (ex.: "#FF6384")</li>
 * </ul>
 */
public record GraficoPizzaFatiaDTO(
        String nomeItem,
        Long idItem,
        double porcentagem,
        String porcentagemFormatada,
        String valorItem,
        BigDecimal valorItemBruto,
        String valorTotal,
        String cor
) {}


