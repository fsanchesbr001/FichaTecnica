package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.enums.TipoRelatorio;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Serviço responsável pela geração de relatórios em formato PDF (A4 retrato).
 *
 * <p>Estrutura do PDF:</p>
 * <ul>
 *   <li><b>Cabeçalho</b> (repete em todas as páginas): título centralizado em negrito 14pt,
 *       número da página alinhado à direita, nomes das colunas em negrito 12pt e
 *       linha separadora.</li>
 *   <li><b>Detalhe</b>: registros extraídos do JSON, um por linha, 12pt.</li>
 *   <li><b>Rodapé</b> (repete em todas as páginas): linha separadora, data/hora
 *       alinhada à esquerda e texto "Emitido pelo Ficha Técnica Ollivander" centralizado.</li>
 * </ul>
 */
@Service
public class RelatorioService {

    private static final Logger logger = LogManager.getLogger(RelatorioService.class);

    // ── Layout constants ──────────────────────────────────────────────────────
    /** Margem horizontal (esquerda e direita) em pontos. */
    private static final float MARGIN_HORIZ   = 36f;
    /** Margem superior do corpo do documento (reserva espaço para o cabeçalho). */
    private static final float MARGIN_TOP     = 95f;
    /** Margem inferior do corpo do documento (reserva espaço para o rodapé). */
    private static final float MARGIN_BOTTOM  = 72f;
    /** Tamanho da fonte do título (pt). */
    private static final float FONT_TITLE     = 14f;
    /** Tamanho da fonte do corpo (pt). */
    private static final float FONT_BODY      = 12f;
    /** Preenchimento interno de cada célula da tabela de dados (pt). */
    private static final float CELL_PADDING   = 4f;
    /** Cor de fundo usada nas linhas pares quando alternarCores = true (LISTA). */
    private static final DeviceRgb LISTA_ROW_ALT_COLOR = new DeviceRgb(230, 244, 234);
    /** Largura minima estimada para cada bloco label/valor no DETALHE (pt). */
    private static final float DETAIL_MIN_FIELD_WIDTH = 240f;
    /** Limite maximo de campos por linha no DETALHE para manter legibilidade. */
    private static final int DETAIL_MAX_FIELDS_PER_ROW = 3;

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gera o relatório PDF em memória e retorna o conteúdo como array de bytes.
     *
     * @param request dados da requisição: JSON, título e mapeamento de colunas
     * @return array de bytes do PDF gerado
     * @throws IOException se ocorrer erro ao criar as fontes ou escrever o PDF
     */
    public byte[] gerarRelatorioPDF(RelatorioRequestDTO request) throws IOException {
        return gerarRelatorioPDF(
                request,
                request.tipoRelatorio(),
                request.orientacao(),
                request.alternarCores()
        );
    }

    /**
     * Gera o relatório PDF em memória e retorna o conteúdo como array de bytes.
     *
     * @param request dados da requisição: JSON, título e mapeamento de colunas
     * @param tipoRelatorio tipo do relatório (LISTA/DETALHE)
     * @param orientacao orientação do relatório (RETRATO/PAISAGEM)
     * @param alternarCores alterna cores das linhas (apenas LISTA)
     * @return array de bytes do PDF gerado
     * @throws IOException se ocorrer erro ao criar as fontes ou escrever o PDF
     */
    public byte[] gerarRelatorioPDF(RelatorioRequestDTO request,
                                    TipoRelatorio tipoRelatorio,
                                    OrientacaoRelatorio orientacao,
                                    Boolean alternarCores) throws IOException {
        logger.info("Iniciando geração de relatório PDF – título: '{}'", request.titulo());

        TipoRelatorio tipo = (tipoRelatorio != null) ? tipoRelatorio : TipoRelatorio.LISTA;
        OrientacaoRelatorio orient = (orientacao != null) ? orientacao : OrientacaoRelatorio.RETRATO;
        boolean alternar = alternarCores != null && alternarCores;

        if (tipo == TipoRelatorio.DETALHE) {
            if (alternar) {
                throw new IllegalArgumentException(
                        "AlternarCores só pode ser true quando tipoRelatorio = LISTA.");
            }
            orient = OrientacaoRelatorio.PAISAGEM;
        }

        // 1. Extrair a lista de registros do JSON
        List<Map<String, String>> registros = extrairLista(request.jsonData(), request.listPath());
        logger.info("Total de registros encontrados: {}", registros.size());

        if (registros.isEmpty()) {
            throw new IllegalArgumentException("Nenhum registro encontrado para gerar o relatório.");
        }

        // 2. Colunas (LinkedHashMap garante a ordem declarada pelo cliente)
        Map<String, String> colunas = request.colunas();
        if (colunas == null) {
            colunas = new LinkedHashMap<>();
        }

        if (tipo == TipoRelatorio.LISTA && colunas.isEmpty()) {
            throw new IllegalArgumentException("Para LISTA, o mapa de colunas deve ser informado.");
        }

        // 3. Criar fontes padrão (Helvetica ≈ Arial, embutida no PDF spec)
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // 4. Gerar PDF em memória
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter   writer  = new PdfWriter(baos);
        PdfDocument pdfDoc  = new PdfDocument(writer);
        PageSize    pageSize = (orient == OrientacaoRelatorio.PAISAGEM) ? PageSize.A4.rotate() : PageSize.A4;
        Document    document = new Document(pdfDoc, pageSize);
        document.setMargins(MARGIN_TOP, MARGIN_HORIZ, MARGIN_BOTTOM, MARGIN_HORIZ);

        // Registrar handler de cabeçalho/rodapé (dispara ao encerrar cada página)
        List<String> headerLabels = (tipo == TipoRelatorio.LISTA)
                ? new ArrayList<>(colunas.values())
                : Collections.emptyList();

        pdfDoc.addEventHandler(
                PdfDocumentEvent.END_PAGE,
                new HeaderFooterHandler(
                        request.titulo(),
                        headerLabels,
                        fontNormal,
                        fontBold
                )
        );

        if (tipo == TipoRelatorio.LISTA) {
            // 5. Montar tabela de dados (sem linha de cabeçalho – fica no evento de página)
            int numCols = colunas.size();
            float[] colWidths = new float[numCols];
            Arrays.fill(colWidths, 1f); // colunas de largura igual

            Table table = new Table(UnitValue.createPercentArray(colWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            int rowIndex = 0;
            for (Map<String, String> registro : registros) {
                rowIndex++;
                boolean pintarLinha = alternar && (rowIndex % 2 == 0);
                for (String fieldKey : colunas.keySet()) {
                    String value = registro.getOrDefault(fieldKey, "");
                    Cell cell = new Cell()
                            .add(new Paragraph(value)
                                    .setFont(fontNormal)
                                    .setFontSize(FONT_BODY))
                            .setBorderTop(null)
                            .setBorderBottom(null)
                            .setBorderLeft(null)
                            .setBorderRight(null)
                            .setPaddingTop(CELL_PADDING)
                            .setPaddingBottom(CELL_PADDING)
                            .setPaddingLeft(CELL_PADDING)
                            .setPaddingRight(CELL_PADDING);

                    if (pintarLinha) {
                        cell.setBackgroundColor(LISTA_ROW_ALT_COLOR);
                    }
                    table.addCell(cell);
                }
            }

            document.add(table);
        } else {
            Map<String, String> base = registros.get(0);
            if (registros.size() > 1) {
                logger.warn("Relatorio DETALHE recebeu {} registros; usando o primeiro.", registros.size());
            }

            Map<String, String> camposDetalhe = new LinkedHashMap<>();
            if (!colunas.isEmpty()) {
                for (Map.Entry<String, String> entry : colunas.entrySet()) {
                    String key = entry.getKey();
                    String label = entry.getValue();
                    camposDetalhe.put(label, base.getOrDefault(key, ""));
                }
            } else {
                for (Map.Entry<String, String> entry : base.entrySet()) {
                    camposDetalhe.put(entry.getKey(), entry.getValue());
                }
            }

            Table detailTable = montarTabelaDetalhe(camposDetalhe, fontNormal, fontBold, pageSize);
            document.add(detailTable);
        }
        document.close(); // também fecha pdfDoc e writer

        logger.info("PDF gerado com sucesso – tamanho: {} bytes", baos.size());
        return baos.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Extração do JSON
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Navega pelo JSON e retorna a lista de registros como
     * {@code List<Map<String,String>>}.
     *
     * @param jsonData  JSON em formato String
     * @param listPath  Caminho separado por "." até o array (vazio = raiz)
     * @return lista de registros como mapas chave→valor (ambos String)
     */
    private List<Map<String, String>> extrairLista(String jsonData, String listPath) {
        JsonElement root = JsonParser.parseString(jsonData);
        JsonArray   jsonArray;

        if (listPath == null || listPath.trim().isEmpty()) {
            if (!root.isJsonArray()) {
                throw new IllegalArgumentException(
                        "O JSON raiz não é um array e nenhum listPath foi informado.");
            }
            jsonArray = root.getAsJsonArray();
        } else {
            JsonElement current = root;
            for (String key : listPath.split("\\.")) {
                if (!current.isJsonObject()) {
                    throw new IllegalArgumentException(
                            "Não foi possível navegar pelo caminho: " + listPath);
                }
                current = current.getAsJsonObject().get(key);
                if (current == null) {
                    throw new IllegalArgumentException(
                            "Chave '" + key + "' não encontrada no JSON.");
                }
            }
            if (!current.isJsonArray()) {
                throw new IllegalArgumentException(
                        "O caminho '" + listPath + "' não aponta para um array.");
            }
            jsonArray = current.getAsJsonArray();
        }

        List<Map<String, String>> result = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            if (element.isJsonObject()) {
                Map<String, String> record = new LinkedHashMap<>();
                JsonObject obj = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    JsonElement val = entry.getValue();
                    record.put(entry.getKey(), formatarValor(entry.getKey(), val));
                }
                result.add(record);
            }
        }
        return result;
    }

    private String formatarValor(String key, JsonElement val) {
        if (val == null || val.isJsonNull()) {
            return "";
        }

        if (val.isJsonPrimitive() && val.getAsJsonPrimitive().isBoolean()) {
            return val.getAsBoolean() ? "Sim" : "Não";
        }

        // Objetos aninhados (ex.: UnidadeMedida): tenta extrair campo "nome", senão "sigla", senão toString compacto
        if (val.isJsonObject()) {
            JsonObject nested = val.getAsJsonObject();
            if (nested.has("nome") && !nested.get("nome").isJsonNull()) {
                return nested.get("nome").getAsString();
            }
            if (nested.has("sigla") && !nested.get("sigla").isJsonNull()) {
                return nested.get("sigla").getAsString();
            }
            // fallback: representação compacta do objeto
            return val.toString();
        }

        String raw = val.getAsString();
        if (raw == null || raw.isBlank()) {
            return "";
        }

        String cpfFormatado = tentarFormatarCpf(key, raw);
        if (cpfFormatado != null) {
            return cpfFormatado;
        }

        String dataFormatada = tentarFormatarDataHora(raw);
        if (dataFormatada != null) {
            return dataFormatada;
        }

        return raw;
    }

    private String tentarFormatarCpf(String key, String raw) {
        if (key == null || !key.toLowerCase(Locale.ROOT).contains("cpf")) {
            return null;
        }

        String digits = raw.replaceAll("\\D", "");
        if (digits.length() != 11) {
            return raw;
        }

        return String.format("%s.%s.%s-%s",
                digits.substring(0, 3),
                digits.substring(3, 6),
                digits.substring(6, 9),
                digits.substring(9, 11));
    }

    private String tentarFormatarDataHora(String raw) {
        DateTimeFormatter target = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        try {
            OffsetDateTime odt = OffsetDateTime.parse(raw);
            return odt.format(target);
        } catch (DateTimeParseException ignored) {
            // try next
        }

        try {
            LocalDateTime ldt = LocalDateTime.parse(raw);
            return ldt.format(target);
        } catch (DateTimeParseException ignored) {
            // try next
        }

        try {
            LocalDate date = LocalDate.parse(raw);
            return date.atStartOfDay().format(target);
        } catch (DateTimeParseException ignored) {
            // try next
        }

        try {
            Instant instant = Instant.parse(raw);
            return instant.atZone(ZoneId.systemDefault()).format(target);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private Table montarTabelaDetalhe(Map<String, String> campos,
                                      PdfFont fontNormal,
                                      PdfFont fontBold,
                                      PageSize pageSize) {
        int fieldsPerRow = calcularCamposPorLinha(pageSize);
        float[] colWidths = new float[fieldsPerRow];
        Arrays.fill(colWidths, 1f);

        Table table = new Table(UnitValue.createPercentArray(colWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        int colIndex = 0;
        for (Map.Entry<String, String> entry : campos.entrySet()) {
            String label = entry.getKey();
            String value = entry.getValue();

            Cell cell = new Cell()
                    .add(new Paragraph(label)
                            .setFont(fontBold)
                            .setFontSize(FONT_BODY))
                    .add(new Paragraph(value == null ? "" : value)
                            .setFont(fontNormal)
                            .setFontSize(FONT_BODY))
                    .setBorderTop(null)
                    .setBorderBottom(null)
                    .setBorderLeft(null)
                    .setBorderRight(null)
                    .setPaddingTop(CELL_PADDING)
                    .setPaddingBottom(CELL_PADDING)
                    .setPaddingLeft(CELL_PADDING)
                    .setPaddingRight(CELL_PADDING);

            table.addCell(cell);
            colIndex++;
        }

        int remainder = colIndex % fieldsPerRow;
        if (remainder != 0) {
            for (int i = remainder; i < fieldsPerRow; i++) {
                table.addCell(new Cell()
                        .setBorderTop(null)
                        .setBorderBottom(null)
                        .setBorderLeft(null)
                        .setBorderRight(null));
            }
        }

        return table;
    }

    private int calcularCamposPorLinha(PageSize pageSize) {
        float contentWidth = pageSize.getWidth() - (MARGIN_HORIZ * 2f);
        int calc = (int) Math.floor(contentWidth / DETAIL_MIN_FIELD_WIDTH);
        if (calc < 2) {
            return 2;
        }
        return Math.min(calc, DETAIL_MAX_FIELDS_PER_ROW);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Event handler: Cabeçalho e Rodapé
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handler de eventos de página que desenha o cabeçalho e o rodapé em cada
     * página do documento PDF.
     *
     * <p><b>Cabeçalho:</b></p>
     * <ul>
     *   <li>Título centralizado em negrito 14pt + "Página: N" alinhado à direita</li>
     *   <li>Nomes das colunas em negrito 12pt, centralizados em cada coluna</li>
     *   <li>Linha separadora horizontal</li>
     * </ul>
     *
     * <p><b>Rodapé:</b></p>
     * <ul>
     *   <li>Linha separadora horizontal</li>
     *   <li>Data/hora alinhada à esquerda (fuso America/Sao_Paulo)</li>
     *   <li>"Emitido pelo Ficha Técnica Ollivander" centralizado</li>
     * </ul>
     */
    private static class HeaderFooterHandler implements IEventHandler {

        private static final Logger log = LogManager.getLogger(HeaderFooterHandler.class);

        private final String        titulo;
        private final List<String>  columnLabels;
        private final PdfFont       fontNormal;
        private final PdfFont       fontBold;

        HeaderFooterHandler(String titulo, List<String> columnLabels,
                            PdfFont fontNormal, PdfFont fontBold) {
            this.titulo       = titulo;
            this.columnLabels = columnLabels;
            this.fontNormal   = fontNormal;
            this.fontBold     = fontBold;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent  = (PdfDocumentEvent) event;
            PdfDocument      pdf       = docEvent.getDocument();
            PdfPage          page      = docEvent.getPage();
            int              pageNumber = pdf.getPageNumber(page);
            Rectangle        pageSize  = page.getPageSize();

            float leftX        = MARGIN_HORIZ;
            float rightX       = pageSize.getWidth() - MARGIN_HORIZ;
            float contentWidth = rightX - leftX;

            try {
                // Novo content stream inserido ANTES do conteúdo da página
                PdfCanvas canvas = new PdfCanvas(
                        page.newContentStreamBefore(),
                        page.getResources(),
                        pdf
                );
                canvas.saveState();

                // ── CABEÇALHO ────────────────────────────────────────────────

                // y base do cabeçalho (imediatamente abaixo da margem superior da folha)
                float headerTop  = pageSize.getTop() - MARGIN_HORIZ;
                float titleY     = headerTop - FONT_TITLE - 4f;

                // Título – centralizado, negrito 14pt
                float titleTextWidth = fontBold.getWidth(titulo, FONT_TITLE);
                float titleX         = leftX + (contentWidth - titleTextWidth) / 2f;
                canvas.beginText()
                        .setFontAndSize(fontBold, FONT_TITLE)
                        .moveText(titleX, titleY)
                        .showText(titulo)
                        .endText();

                // "Página: N" – alinhado à direita, normal 12pt
                String pageText      = "Página: " + pageNumber;
                float  pageTextWidth = fontNormal.getWidth(pageText, FONT_BODY);
                canvas.beginText()
                        .setFontAndSize(fontNormal, FONT_BODY)
                        .moveText(rightX - pageTextWidth, titleY)
                        .showText(pageText)
                        .endText();

                if (!columnLabels.isEmpty()) {
                    // Cabeçalhos das colunas – negrito 12pt, centralizados em cada coluna
                    float colHeaderY = titleY - FONT_BODY - 10f;
                    int   numCols    = columnLabels.size();
                    float colWidth   = contentWidth / numCols;

                    for (int i = 0; i < numCols; i++) {
                        String label  = columnLabels.get(i);
                        float  labelX = leftX + i * colWidth + CELL_PADDING;
                        canvas.beginText()
                                .setFontAndSize(fontBold, FONT_BODY)
                                .moveText(labelX, colHeaderY)
                                .showText(label)
                                .endText();
                    }

                    // Linha separadora abaixo dos cabeçalhos das colunas
                    float headerLineY = colHeaderY - 8f;
                    canvas.setLineWidth(0.5f)
                            .moveTo(leftX,  headerLineY)
                            .lineTo(rightX, headerLineY)
                            .stroke();
                }

                // ── RODAPÉ ───────────────────────────────────────────────────

                // Linha separadora do rodapé
                float footerLineY = MARGIN_HORIZ + 30f;
                canvas.setLineWidth(0.5f)
                        .moveTo(leftX,  footerLineY)
                        .lineTo(rightX, footerLineY)
                        .stroke();

                // Textos do rodapé
                float footerTextY = footerLineY - FONT_BODY - 4f;

                // Data/hora – alinhada à esquerda
                String dataAtual = formatarDataAtual();
                canvas.beginText()
                        .setFontAndSize(fontNormal, FONT_BODY)
                        .moveText(leftX, footerTextY)
                        .showText(dataAtual)
                        .endText();

                // "Emitido pelo..." – centralizado
                String emitterText  = "Emitido pelo Ficha Técnica Ollivander";
                float  emitterWidth = fontNormal.getWidth(emitterText, FONT_BODY);
                float  emitterX     = leftX + (contentWidth - emitterWidth) / 2f;
                canvas.beginText()
                        .setFontAndSize(fontNormal, FONT_BODY)
                        .moveText(emitterX, footerTextY)
                        .showText(emitterText)
                        .endText();

                canvas.restoreState();
                canvas.release();

            } catch (Exception e) {
                log.error("Erro ao desenhar cabeçalho/rodapé na página {}", pageNumber, e);
            }
        }

        /**
         * Retorna a data/hora atual formatada com o dia da semana em português,
         * usando o fuso horário America/Sao_Paulo.
         * Exemplo: "Quinta-feira, 13/03/2026 14:30:00"
         */
        private String formatarDataAtual() {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
            return now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        }
    }
}
