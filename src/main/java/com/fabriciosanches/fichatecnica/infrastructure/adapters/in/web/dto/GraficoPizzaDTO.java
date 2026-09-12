package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

import java.util.List;

/**
 * DTO de saÃ­da do endpoint de grÃ¡fico de pizza de composiÃ§Ã£o de custo de um Produto.
 * Estruturado para consumo direto pelo Chart.js via ng2-charts no Angular.
 *
 * <ul>
 *   <li>{@code nomeProduto}  â€“ nome do produto selecionado</li>
 *   <li>{@code valorTotal}   â€“ valor total do produto formatado (ex.: "R$ 15,50")</li>
 *   <li>{@code fatias}       â€“ lista de fatias com dados completos para tooltip</li>
 *   <li>{@code labels}       â€“ nomes dos itens na mesma ordem das fatias (eixo do Chart.js)</li>
 *   <li>{@code valores}      â€“ percentuais brutos na mesma ordem das fatias (dataset do Chart.js)</li>
 *   <li>{@code cores}        â€“ cores hexadecimais na mesma ordem das fatias (backgroundColor)</li>
 * </ul>
 *
 * <p>ConfiguraÃ§Ã£o sugerida no Angular (ng2-charts / Chart.js):</p>
 * <pre>
 *   Tipo de grÃ¡fico : 'pie' ou 'doughnut'
 *   labels          : response.labels
 *   datasets[0].data: response.valores
 *   datasets[0].backgroundColor: response.cores
 *
 *   tooltip customizado:
 *     const fatia = response.fatias[tooltipItem.dataIndex];
 *     return [
 *       `ParticipaÃ§Ã£o: ${fatia.porcentagemFormatada}`,
 *       `Valor do item: ${fatia.valorItem}`,
 *       `Valor total: ${fatia.valorTotal}`
 *     ];
 * </pre>
 */
public record GraficoPizzaDTO(
        String nomeProduto,
        String valorTotal,
        List<GraficoPizzaFatiaDTO> fatias,
        List<String> labels,
        List<Double> valores,
        List<String> cores
) {}


