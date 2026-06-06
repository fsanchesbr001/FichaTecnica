package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Serviço responsável pela geração de gráficos como imagens PNG.
 * Utiliza JFreeChart para renderização.
 */
@Service
public class GraficoService {

    private static final Logger logger = LogManager.getLogger(GraficoService.class);

    /** Largura padrão da imagem gerada (pixels). */
    private static final int CHART_WIDTH  = 900;
    /** Altura padrão da imagem gerada (pixels). */
    private static final int CHART_HEIGHT = 400;

    /** Cor da linha do gráfico – azul escuro. */
    private static final Color COR_LINHA  = new Color(21, 101, 192);   // #1565C0
    /** Cor dos pontos (círculos) – vermelho. */
    private static final Color COR_PONTOS = new Color(211, 47, 47);    // #D32F2F

    /**
     * Gera um gráfico de linha de variação de preços a partir de um {@link GraficoPrecoItemDTO}
     * e retorna os bytes PNG da imagem resultante.
     *
     * <ul>
     *   <li>Eixo X – datas formatadas (labels do DTO)</li>
     *   <li>Eixo Y – valores numéricos brutos</li>
     *   <li>Linha azul (#1565C0) contínua</li>
     *   <li>Pontos de intersecção: círculos vermelhos (#D32F2F)</li>
     * </ul>
     *
     * @param dto dados do gráfico gerados pelo {@link HistoricoItemService}
     * @return array de bytes da imagem PNG
     * @throws IOException se ocorrer erro ao serializar o gráfico
     */
    public byte[] gerarGraficoPNG(GraficoPrecoItemDTO dto) throws IOException {
        logger.info("Gerando gráfico PNG para item '{}'", dto.nomeItem());

        // ── Dataset ──────────────────────────────────────────────────────────
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<String>     labels  = dto.labels();
        List<BigDecimal> valores = dto.valores();

        for (int i = 0; i < labels.size(); i++) {
            BigDecimal val = (valores.get(i) != null) ? valores.get(i) : BigDecimal.ZERO;
            dataset.addValue(val, dto.nomeItem(), labels.get(i));
        }

        // ── Gráfico ───────────────────────────────────────────────────────────
        JFreeChart chart = ChartFactory.createLineChart(
                dto.titulo(),          // título
                "Data",                // label eixo X
                "Valor (R$)",          // label eixo Y
                dataset,
                PlotOrientation.VERTICAL,
                false,                 // sem legenda lateral
                true,                  // tooltips
                false                  // urls
        );

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 14));

        // ── Plot ─────────────────────────────────────────────────────────────
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setDomainGridlinesVisible(true);
        plot.setOutlineVisible(false);

        // ── Eixo X ────────────────────────────────────────────────────────────
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        domainAxis.setAxisLineVisible(true);

        // ── Eixo Y ────────────────────────────────────────────────────────────
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        rangeAxis.setNumberFormatOverride(
                java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR")));

        // ── Renderer: linha azul + pontos vermelhos ───────────────────────────
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, COR_LINHA);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-5, -5, 10, 10));
        renderer.setSeriesShapesFilled(0, true);
        renderer.setSeriesFillPaint(0, COR_PONTOS);
        renderer.setUseFillPaint(true);
        renderer.setSeriesOutlinePaint(0, COR_PONTOS);
        renderer.setDrawOutlines(true);
        renderer.setDefaultToolTipGenerator(
                (categoryDataset, row, col) -> {
                    String label      = labels.get(col);
                    String valorFmt   = dto.valoresFormatados().get(col);
                    String variacao   = dto.variacoes().get(col);
                    String varMon     = dto.variacoesMonetarias().get(col);
                    return label + " – " + valorFmt + " | " + variacao + " (" + varMon + ")";
                }
        );
        plot.setRenderer(renderer);

        // Subtítulo com nome do item
        chart.addSubtitle(new TextTitle(
                dto.nomeItem(),
                new Font("SansSerif", Font.ITALIC, 11)
        ));

        // ── Renderizar como PNG ───────────────────────────────────────────────
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, CHART_WIDTH, CHART_HEIGHT);
        byte[] pngBytes = baos.toByteArray();

        logger.info("Gráfico PNG gerado com sucesso – {} bytes, {} pontos", pngBytes.length, labels.size());
        return pngBytes;
    }

    /**
     * Gera um gráfico de pizza (composição de custo de produto) e retorna os bytes PNG.
     */
    public byte[] gerarGraficoPizzaPNG(GraficoPizzaDTO dto) throws IOException {
        logger.info("Gerando gráfico de pizza PNG para produto '{}'", dto.nomeProduto());

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        List<String> labels = dto.labels() != null ? dto.labels() : List.of();
        List<Double> valores = dto.valores() != null ? dto.valores() : List.of();

        for (int i = 0; i < labels.size(); i++) {
            Double valor = i < valores.size() && valores.get(i) != null ? valores.get(i) : 0d;
            dataset.setValue(labels.get(i), valor);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Composição de Custo - " + dto.nomeProduto(),
                dataset,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 14));
        chart.addSubtitle(new TextTitle("Total de Itens: " + dto.valorTotal(), new Font("SansSerif", Font.PLAIN, 11)));

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        plot.setLabelGap(0.02);
        plot.setSimpleLabels(true);

        List<String> cores = dto.cores() != null ? dto.cores() : List.of();
        for (int i = 0; i < labels.size(); i++) {
            if (i < cores.size() && cores.get(i) != null) {
                plot.setSectionPaint(labels.get(i), Color.decode(cores.get(i)));
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, CHART_WIDTH, CHART_HEIGHT);
        byte[] pngBytes = baos.toByteArray();

        logger.info("Gráfico de pizza PNG gerado com sucesso – {} bytes, {} fatias", pngBytes.length, labels.size());
        return pngBytes;
    }
}

