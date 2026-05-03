package com.fabriciosanches.fichatecnica.dtos;

import java.math.BigDecimal;

/**
 * Representa uma fatia do gráfico de pizza de composição de custo de um Produto.
 *
 * <ul>
 *   <li>{@code nomeItem}            – nome do Item</li>
 *   <li>{@code idItem}              – código do Item (para navegação no frontend)</li>
 *   <li>{@code porcentagem}         – percentual bruto desta fatia sobre o valor total (ex.: 32.5)</li>
 *   <li>{@code porcentagemFormatada}– percentual formatado para exibição (ex.: "32,5%")</li>
 *   <li>{@code valorItem}           – valor monetário formatado deste item (ex.: "R$ 5,04")</li>
 *   <li>{@code valorItemBruto}      – valor numérico bruto deste item</li>
 *   <li>{@code valorTotal}          – valor total do produto formatado (ex.: "R$ 15,50")</li>
 *   <li>{@code cor}                 – cor hexadecimal atribuída a esta fatia (ex.: "#FF6384")</li>
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

