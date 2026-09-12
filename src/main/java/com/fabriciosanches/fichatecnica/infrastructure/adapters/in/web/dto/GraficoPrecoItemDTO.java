package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de saÃ­da do endpoint de grÃ¡fico de variaÃ§Ã£o de preÃ§os de um Item.
 * Estruturado para consumo direto pelo Chart.js via ng2-charts no Angular.
 *
 * <ul>
 *   <li>{@code titulo}             â€“ rÃ³tulo do grÃ¡fico (ex.: "VariaÃ§Ã£o de PreÃ§o â€“ Arroz")</li>
 *   <li>{@code nomeItem}           â€“ nome do Item buscado</li>
 *   <li>{@code labels}             â€“ datas no eixo X, formatadas como "dd/MM/yyyy"</li>
 *   <li>{@code valores}            â€“ valores numÃ©ricos brutos para o eixo Y (BigDecimal)</li>
 *   <li>{@code valoresFormatados}  â€“ valores no formato monetÃ¡rio BR para exibiÃ§Ã£o no tooltip</li>
 *   <li>{@code variacoes}          â€“ variaÃ§Ã£o percentual em relaÃ§Ã£o ao ponto anterior
 *                                    (ex.: "+5,0%", "-3,2%"); primeiro ponto retorna "â€”"</li>
 *   <li>{@code variacoesMonetarias}â€“ variaÃ§Ã£o monetÃ¡ria em relaÃ§Ã£o ao ponto anterior
 *                                    (ex.: "+R$ 0,50", "-R$ 1,25"); primeiro ponto retorna "â€”"</li>
 * </ul>
 *
 * <p>ConfiguraÃ§Ã£o sugerida no Angular (ng2-charts / Chart.js):</p>
 * <pre>
 *   Tipo de grÃ¡fico      : 'line'
 *   borderColor          : '#1565C0'    (linha azul)
 *   pointBackgroundColor : '#D32F2F'   (pontos vermelhos)
 *   tooltip customizado  : `${labels[i]} â€“ ${valoresFormatados[i]} | ${variacoes[i]} (${variacoesMonetarias[i]})`
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

