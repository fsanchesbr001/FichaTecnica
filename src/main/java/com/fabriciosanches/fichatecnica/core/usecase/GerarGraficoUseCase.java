package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.GraficoPrecoItemDTO;
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

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ServiÃ§o responsÃ¡vel pela geraÃ§Ã£o de grÃ¡ficos como imagens PNG.
 * Utiliza JFreeChart para renderizaÃ§Ã£o.
 */
public class GerarGraficoUseCase implements GerarGraficoPort {

    private static final Logger logger = LogManager.getLogger(GerarGraficoUseCase.class);

    /** Largura padrÃ£o da imagem gerada (pixels). */
    private static final int CHART_WIDTH  = 900;
    /** Altura padrÃ£o da imagem gerada (pixels). */
    private static final int CHART_HEIGHT = 400;

    /** Cor da linha do grÃ¡fico â€“ azul escuro. */
    private static final Color COR_LINHA  = new Color(21, 101, 192);   // #1565C0
    /** Cor dos pontos (cÃ­rculos) â€“ vermelho. */
    private static final Color COR_PONTOS = new Color(211, 47, 47);    // #D32F2F
    private static final Pattern LABEL_EVENTO_PATTERN = Pattern.compile("^(.*?)(?:\\s*\\[#(\\d+)])?$");

    /**
     * Gera um grÃ¡fico de linha de variaÃ§Ã£o de preÃ§os a partir de um {@link GraficoPrecoItemDTO}
     * e retorna os bytes PNG da imagem resultante.
     *
     * <ul>
     *   <li>Eixo X â€“ datas formatadas (labels do DTO)</li>
     *   <li>Eixo Y â€“ valores numÃ©ricos brutos</li>
     *   <li>Linha azul (#1565C0) contÃ­nua</li>
     *   <li>Pontos de intersecÃ§Ã£o: cÃ­rculos vermelhos (#D32F2F)</li>
     * </ul>
     *
     * @param dto dados do grÃ¡fico gerados pelo fluxo de histÃ³rico de item (use case)
     * @return array de bytes da imagem PNG
     * @throws IOException se ocorrer erro ao serializar o grÃ¡fico
     */
    public byte[] gerarGraficoPNG(GraficoPrecoItemDTO dto) throws IOException {
        logger.info("Gerando grÃ¡fico PNG para item '{}'", dto.nomeItem());

        // â”€â”€ Dataset â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<String>     labels  = dto.labels();
        List<BigDecimal> valores = dto.valores();
        List<EventoInfo> eventos = new ArrayList<>(labels.size());

        for (int i = 0; i < labels.size(); i++) {
            EventoInfo evento = parseEventoInfo(labels.get(i), i);
            eventos.add(evento);
            BigDecimal val = (valores.get(i) != null) ? valores.get(i) : BigDecimal.ZERO;
            dataset.addValue(val, dto.nomeItem(), new EventoCategoria(evento.data(), evento.codigoEvento(), i));
        }

        // â”€â”€ GrÃ¡fico â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JFreeChart chart = ChartFactory.createLineChart(
                dto.titulo(),          // tÃ­tulo
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

        // â”€â”€ Plot â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setDomainGridlinesVisible(true);
        plot.setOutlineVisible(false);

        // â”€â”€ Eixo X â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        domainAxis.setAxisLineVisible(true);

        // â”€â”€ Eixo Y â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        rangeAxis.setNumberFormatOverride(
                java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR")));

        // â”€â”€ Renderer: linha azul + pontos vermelhos â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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
                    EventoInfo evento = eventos.get(col);
                    String label      = evento.data();
                    String codigoInfo = evento.codigoEvento() == null ? "" : " | #" + evento.codigoEvento();
                    String valorFmt   = dto.valoresFormatados().get(col);
                    String variacao   = dto.variacoes().get(col);
                    String varMon     = dto.variacoesMonetarias().get(col);
                    return label + " â€“ " + valorFmt + codigoInfo + " | " + variacao + " (" + varMon + ")";
                }
        );
        plot.setRenderer(renderer);

        // SubtÃ­tulo com nome do item
        chart.addSubtitle(new TextTitle(
                dto.nomeItem(),
                new Font("SansSerif", Font.ITALIC, 11)
        ));

        // â”€â”€ Renderizar como PNG â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, CHART_WIDTH, CHART_HEIGHT);
        byte[] pngBytes = baos.toByteArray();

        logger.info("GrÃ¡fico PNG gerado com sucesso â€“ {} bytes, {} pontos", pngBytes.length, labels.size());
        return pngBytes;
    }


    /**
     * Gera um grÃ¡fico de pizza (composiÃ§Ã£o de custo de produto) e retorna os bytes PNG.
     */
    public byte[] gerarGraficoPizzaPNG(GraficoPizzaDTO dto) throws IOException {
        logger.info("Gerando grÃ¡fico de pizza PNG para produto '{}'", dto.nomeProduto());

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        List<String> labels = dto.labels() != null ? dto.labels() : List.of();
        List<Double> valores = dto.valores() != null ? dto.valores() : List.of();

        for (int i = 0; i < labels.size(); i++) {
            Double valor = i < valores.size() && valores.get(i) != null ? valores.get(i) : 0d;
            dataset.setValue(labels.get(i), valor);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "ComposiÃ§Ã£o de Custo - " + dto.nomeProduto(),
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

        logger.info("GrÃ¡fico de pizza PNG gerado com sucesso â€“ {} bytes, {} fatias", pngBytes.length, labels.size());
        return pngBytes;
    }

    private EventoInfo parseEventoInfo(String labelOriginal, int fallbackOrdem) {
        String base = labelOriginal != null ? labelOriginal : "";
        Matcher matcher = LABEL_EVENTO_PATTERN.matcher(base.trim());
        if (!matcher.matches()) {
            return new EventoInfo(base, (long) fallbackOrdem);
        }

        String data = matcher.group(1) != null ? matcher.group(1).trim() : "";
        String codigoTexto = matcher.group(2);
        if (codigoTexto == null || codigoTexto.isBlank()) {
            return new EventoInfo(data, (long) fallbackOrdem);
        }

        try {
            return new EventoInfo(data, Long.parseLong(codigoTexto));
        } catch (NumberFormatException ignored) {
            return new EventoInfo(data, (long) fallbackOrdem);
        }
    }

    private record EventoInfo(String data, Long codigoEvento) {}

    private record EventoCategoria(String dataLabel, Long codigoEvento, int ordemCadastro)
            implements Comparable<EventoCategoria> {
        @Override
        public int compareTo(EventoCategoria other) {
            return Integer.compare(this.ordemCadastro, other.ordemCadastro);
        }

        @Override
        public String toString() {
            return dataLabel;
        }
    }
}



