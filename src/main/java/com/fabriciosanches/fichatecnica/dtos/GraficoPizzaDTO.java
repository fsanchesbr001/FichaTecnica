package com.fabriciosanches.fichatecnica.dtos;

import java.util.List;

/**
 * DTO de saída do endpoint de gráfico de pizza de composição de custo de um Produto.
 * Estruturado para consumo direto pelo Chart.js via ng2-charts no Angular.
 *
 * <ul>
 *   <li>{@code nomeProduto}  – nome do produto selecionado</li>
 *   <li>{@code valorTotal}   – valor total do produto formatado (ex.: "R$ 15,50")</li>
 *   <li>{@code fatias}       – lista de fatias com dados completos para tooltip</li>
 *   <li>{@code labels}       – nomes dos itens na mesma ordem das fatias (eixo do Chart.js)</li>
 *   <li>{@code valores}      – percentuais brutos na mesma ordem das fatias (dataset do Chart.js)</li>
 *   <li>{@code cores}        – cores hexadecimais na mesma ordem das fatias (backgroundColor)</li>
 * </ul>
 *
 * <p>Configuração sugerida no Angular (ng2-charts / Chart.js):</p>
 * <pre>
 *   Tipo de gráfico : 'pie' ou 'doughnut'
 *   labels          : response.labels
 *   datasets[0].data: response.valores
 *   datasets[0].backgroundColor: response.cores
 *
 *   tooltip customizado:
 *     const fatia = response.fatias[tooltipItem.dataIndex];
 *     return [
 *       `Participação: ${fatia.porcentagemFormatada}`,
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

