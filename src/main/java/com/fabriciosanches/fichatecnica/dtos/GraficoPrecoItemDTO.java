package com.fabriciosanches.fichatecnica.dtos;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de saída do endpoint de gráfico de variação de preços de um Item.
 * Estruturado para consumo direto pelo Chart.js via ng2-charts no Angular.
 *
 * <ul>
 *   <li>{@code titulo}             – rótulo do gráfico (ex.: "Variação de Preço – Arroz")</li>
 *   <li>{@code nomeItem}           – nome do Item buscado</li>
 *   <li>{@code labels}             – datas no eixo X, formatadas como "dd/MM/yyyy"</li>
 *   <li>{@code valores}            – valores numéricos brutos para o eixo Y (BigDecimal)</li>
 *   <li>{@code valoresFormatados}  – valores no formato monetário BR para exibição no tooltip</li>
 *   <li>{@code variacoes}          – variação percentual em relação ao ponto anterior
 *                                    (ex.: "+5,0%", "-3,2%"); primeiro ponto retorna "—"</li>
 *   <li>{@code variacoesMonetarias}– variação monetária em relação ao ponto anterior
 *                                    (ex.: "+R$ 0,50", "-R$ 1,25"); primeiro ponto retorna "—"</li>
 * </ul>
 *
 * <p>Configuração sugerida no Angular (ng2-charts / Chart.js):</p>
 * <pre>
 *   Tipo de gráfico      : 'line'
 *   borderColor          : '#1565C0'    (linha azul)
 *   pointBackgroundColor : '#D32F2F'   (pontos vermelhos)
 *   tooltip customizado  : `${labels[i]} – ${valoresFormatados[i]} | ${variacoes[i]} (${variacoesMonetarias[i]})`
 * </pre>
 */
public record GraficoPrecoItemDTO(
        String titulo,
        String nomeItem,
        List<String> labels,
        List<BigDecimal> valores,
        List<String> valoresFormatados,
        List<String> variacoes,
        List<String> variacoesMonetarias
) {}
