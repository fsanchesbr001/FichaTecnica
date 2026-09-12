package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.core.domain.enums.ImagemPosicao;
import com.fabriciosanches.fichatecnica.core.domain.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.core.domain.enums.TipoRelatorio;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
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
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * ServiÃ§o responsÃ¡vel pela geraÃ§Ã£o de relatÃ³rios em formato PDF (A4 retrato).
 *
 * <p>Estrutura do PDF:</p>
 * <ul>
 *   <li><b>CabeÃ§alho</b> (repete em todas as pÃ¡ginas): tÃ­tulo centralizado em negrito 14pt,
 *       nÃºmero da pÃ¡gina alinhado Ã  direita, nomes das colunas em negrito 12pt e
 *       linha separadora.</li>
 *   <li><b>Detalhe</b>: registros extraÃ­dos do JSON, um por linha, 12pt.</li>
 *   <li><b>RodapÃ©</b> (repete em todas as pÃ¡ginas): linha separadora, data/hora
 *       alinhada Ã  esquerda e texto "Emitido pelo Ficha TÃ©cnica Ollivander" centralizado.</li>
 * </ul>
 */
public class GerarRelatorioUseCase implements GerarRelatorioPort {

    private static final Logger logger = LogManager.getLogger(GerarRelatorioUseCase.class);

    // â”€â”€ Layout constants â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    /** Margem horizontal (esquerda e direita) em pontos. */
    private static final float MARGIN_HORIZ   = 36f;
    /** Margem superior do corpo do documento (reserva espaÃ§o para o cabeÃ§alho). */
    private static final float MARGIN_TOP     = 95f;
    /** Margem inferior do corpo do documento (reserva espaÃ§o para o rodapÃ©). */
    private static final float MARGIN_BOTTOM  = 72f;
    /** Tamanho da fonte do tÃ­tulo (pt). */
    private static final float FONT_TITLE     = 14f;
    /** Tamanho da fonte do corpo (pt). */
    private static final float FONT_BODY      = 12f;
    /** Preenchimento interno de cada cÃ©lula da tabela de dados (pt). */
    private static final float CELL_PADDING   = 4f;
    /** Cor de fundo usada nas linhas pares quando alternarCores = true (LISTA). */
    private static final DeviceRgb LISTA_ROW_ALT_COLOR = new DeviceRgb(230, 244, 234);
    /** Largura minima estimada para cada bloco label/valor no DETALHE (pt). */
    private static final float DETAIL_MIN_FIELD_WIDTH = 240f;
    /** Limite maximo de campos por linha no DETALHE para manter legibilidade. */
    private static final int DETAIL_MAX_FIELDS_PER_ROW = 3;
    /** Altura mÃ¡xima da imagem no relatÃ³rio (pt). Garante escala proporcional sem prejudicar o layout. */
    private static final float MAX_IMAGE_HEIGHT = 200f;
    /** Cor de fundo do cabeÃ§alho de tabelas auxiliares no DETALHE. */
    private static final DeviceRgb DETAIL_AUX_HEADER_COLOR = new DeviceRgb(240, 240, 240);

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Gera o relatÃ³rio PDF em memÃ³ria e retorna o conteÃºdo como array de bytes.
     *
     * @param request dados da requisiÃ§Ã£o: JSON, tÃ­tulo e mapeamento de colunas
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
     * Gera o relatÃ³rio PDF em memÃ³ria e retorna o conteÃºdo como array de bytes.
     *
     * @param request dados da requisiÃ§Ã£o: JSON, tÃ­tulo e mapeamento de colunas
     * @param tipoRelatorio tipo do relatÃ³rio (LISTA/DETALHE)
     * @param orientacao orientaÃ§Ã£o do relatÃ³rio (RETRATO/PAISAGEM)
     * @param alternarCores alterna cores das linhas (apenas LISTA)
     * @return array de bytes do PDF gerado
     * @throws IOException se ocorrer erro ao criar as fontes ou escrever o PDF
     */
    public byte[] gerarRelatorioPDF(RelatorioRequestDTO request,
                                    TipoRelatorio tipoRelatorio,
                                    OrientacaoRelatorio orientacao,
                                    Boolean alternarCores) throws IOException {
        logger.info("Iniciando geraÃ§Ã£o de relatÃ³rio PDF â€“ tÃ­tulo: '{}'", request.titulo());

        TipoRelatorio tipo = (tipoRelatorio != null) ? tipoRelatorio : TipoRelatorio.LISTA;
        OrientacaoRelatorio orient = (orientacao != null) ? orientacao : OrientacaoRelatorio.RETRATO;
        boolean alternar = alternarCores != null && alternarCores;

        if (tipo == TipoRelatorio.DETALHE) {
            if (alternar) {
                throw new IllegalArgumentException(
                        "AlternarCores sÃ³ pode ser true quando tipoRelatorio = LISTA.");
            }
            orient = OrientacaoRelatorio.PAISAGEM;
        }

        // ValidaÃ§Ãµes de imagem
        boolean comImagem = Boolean.TRUE.equals(request.usarImagem());
        if (comImagem) {
            if (request.imagem() == null || request.imagem().length == 0) {
                throw new IllegalArgumentException(
                        "usarImagem=true exige que o campo 'imagem' seja informado.");
            }
            if (request.imagemPosicao() == null) {
                throw new IllegalArgumentException(
                        "usarImagem=true exige que o campo 'imagemPosicao' seja informado (INICIO ou FIM).");
            }
        }

        boolean comImagemSecundaria = request.imagemSecundaria() != null;
        if (comImagemSecundaria) {
            if (request.imagemSecundaria().length == 0) {
                throw new IllegalArgumentException("'imagemSecundaria' foi informada, mas estÃ¡ vazia.");
            }
            if (request.imagemSecundariaPosicao() == null) {
                throw new IllegalArgumentException(
                        "'imagemSecundaria' exige que 'imagemSecundariaPosicao' seja informada (INICIO ou FIM).");
            }
        }

        // 1. Extrair a lista de registros do JSON
        List<Map<String, String>> registros = extrairLista(request.jsonData(), request.listPath());
        logger.info("Total de registros encontrados: {}", registros.size());

        if (registros.isEmpty()) {
            throw new IllegalArgumentException("Nenhum registro encontrado para gerar o relatÃ³rio.");
        }

        // 2. Colunas (LinkedHashMap garante a ordem declarada pelo cliente)
        Map<String, String> colunas = request.colunas();
        if (colunas == null) {
            colunas = new LinkedHashMap<>();
        }

        if (tipo == TipoRelatorio.LISTA && colunas.isEmpty()) {
            throw new IllegalArgumentException("Para LISTA, o mapa de colunas deve ser informado.");
        }

        // 3. Criar fontes padrÃ£o (Helvetica â‰ˆ Arial, embutida no PDF spec)
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // 4. Gerar PDF em memÃ³ria
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter   writer  = new PdfWriter(baos);
        PdfDocument pdfDoc  = new PdfDocument(writer);
        PageSize    pageSize = (orient == OrientacaoRelatorio.PAISAGEM) ? PageSize.A4.rotate() : PageSize.A4;
        Document    document = new Document(pdfDoc, pageSize);
        document.setMargins(MARGIN_TOP, MARGIN_HORIZ, MARGIN_BOTTOM, MARGIN_HORIZ);

        // Registrar handler de cabeÃ§alho/rodapÃ© (dispara ao encerrar cada pÃ¡gina)
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
            // Imagem no INICIO (antes da tabela)
            adicionarImagensNaPosicao(document, request, pageSize, ImagemPosicao.INICIO, comImagem, comImagemSecundaria);

            // 5. Montar tabela de dados (sem linha de cabeÃ§alho â€“ fica no evento de pÃ¡gina)
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

            // Imagem no FIM (apÃ³s a tabela)
            adicionarImagensNaPosicao(document, request, pageSize, ImagemPosicao.FIM, comImagem, comImagemSecundaria);
        } else {
            Map<String, String> base = registros.get(0);
            if (registros.size() > 1) {
                logger.warn("Relatorio DETALHE recebeu {} registros; usando o primeiro.", registros.size());
            }

            String itensComposicaoTabelaJson = base.get("itensComposicaoTabelaJson");

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

            // Imagem no INICIO (antes da ficha de detalhe)
            adicionarImagensNaPosicao(document, request, pageSize, ImagemPosicao.INICIO, comImagem, comImagemSecundaria);

            Table detailTable = montarTabelaDetalhe(camposDetalhe, fontNormal, fontBold, pageSize);
            document.add(detailTable);

            Table itensComposicaoTable = montarTabelaItensComposicao(itensComposicaoTabelaJson, fontNormal, fontBold);
            if (itensComposicaoTable != null) {
                document.add(new Paragraph("Itens do Produto")
                        .setFont(fontBold)
                        .setFontSize(FONT_BODY)
                        .setMarginTop(12f)
                        .setMarginBottom(6f));
                document.add(itensComposicaoTable);
            }

            // Imagem no FIM (apÃ³s a ficha de detalhe)
            adicionarImagensNaPosicao(document, request, pageSize, ImagemPosicao.FIM, comImagem, comImagemSecundaria);
        }
        document.close(); // tambÃ©m fecha pdfDoc e writer

        logger.info("PDF gerado com sucesso â€“ tamanho: {} bytes", baos.size());
        return baos.toByteArray();
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    //  Imagem
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Cria um elemento {@link Image} centralizado horizontalmente.
     * A imagem Ã© redimensionada proporcionalmente para caber na largura Ãºtil da pÃ¡gina
     * e nÃ£o ultrapassar {@link #MAX_IMAGE_HEIGHT} pontos de altura,
     * preservando o aspect-ratio original.
     *
     * @param imageBytes bytes da imagem (PNG, JPEG etc.)
     * @param pageSize   tamanho da pÃ¡gina atual
     * @return elemento {@link Image} pronto para ser adicionado ao {@link Document}
     */
    private Image criarImagemCentralizada(byte[] imageBytes, PageSize pageSize) throws IOException {
        float maxWidth = pageSize.getWidth() - (MARGIN_HORIZ * 2f);
        ImageData imageData = ImageDataFactory.create(imageBytes);
        Image img = new Image(imageData);

        // scaleToFit redimensiona proporcionalmente para caber na caixa maxWidth Ã— MAX_IMAGE_HEIGHT
        img.scaleToFit(maxWidth, MAX_IMAGE_HEIGHT);

        img.setHorizontalAlignment(HorizontalAlignment.CENTER);
        img.setMarginTop(8f);
        img.setMarginBottom(8f);
        logger.debug("Imagem adicionada ao relatÃ³rio â€“ largura: {}pt, altura: {}pt",
                img.getImageScaledWidth(), img.getImageScaledHeight());
        return img;
    }

    private void adicionarImagensNaPosicao(Document document,
                                           RelatorioRequestDTO request,
                                           PageSize pageSize,
                                           ImagemPosicao posicao,
                                           boolean comImagem,
                                           boolean comImagemSecundaria) throws IOException {
        if (comImagem && request.imagemPosicao() == posicao) {
            document.add(criarImagemCentralizada(request.imagem(), pageSize));
        }
        if (comImagemSecundaria && request.imagemSecundariaPosicao() == posicao) {
            document.add(criarImagemCentralizada(request.imagemSecundaria(), pageSize));
        }
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    //  ExtraÃ§Ã£o do JSON
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Navega pelo JSON e retorna a lista de registros como
     * {@code List<Map<String,String>>}.
     *
     * @param jsonData  JSON em formato String
     * @param listPath  Caminho separado por "." atÃ© o array (vazio = raiz)
     * @return lista de registros como mapas chaveâ†’valor (ambos String)
     */
    private List<Map<String, String>> extrairLista(String jsonData, String listPath) {
        JsonElement root = JsonParser.parseString(jsonData);
        JsonArray   jsonArray;

        if (listPath == null || listPath.trim().isEmpty()) {
            if (!root.isJsonArray()) {
                throw new IllegalArgumentException(
                        "O JSON raiz nÃ£o Ã© um array e nenhum listPath foi informado.");
            }
            jsonArray = root.getAsJsonArray();
        } else {
            JsonElement current = root;
            for (String key : listPath.split("\\.")) {
                if (!current.isJsonObject()) {
                    throw new IllegalArgumentException(
                            "NÃ£o foi possÃ­vel navegar pelo caminho: " + listPath);
                }
                current = current.getAsJsonObject().get(key);
                if (current == null) {
                    throw new IllegalArgumentException(
                            "Chave '" + key + "' nÃ£o encontrada no JSON.");
                }
            }
            if (!current.isJsonArray()) {
                throw new IllegalArgumentException(
                        "O caminho '" + listPath + "' nÃ£o aponta para um array.");
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
            return val.getAsBoolean() ? "Sim" : "NÃ£o";
        }

        // Objetos aninhados (ex.: UnidadeMedida): tenta extrair campo "nome", senÃ£o "sigla", senÃ£o toString compacto
        if (val.isJsonObject()) {
            JsonObject nested = val.getAsJsonObject();
            if (nested.has("nome") && !nested.get("nome").isJsonNull()) {
                return nested.get("nome").getAsString();
            }
            if (nested.has("sigla") && !nested.get("sigla").isJsonNull()) {
                return nested.get("sigla").getAsString();
            }
            // fallback: representaÃ§Ã£o compacta do objeto
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

    private Table montarTabelaItensComposicao(String itensJson,
                                              PdfFont fontNormal,
                                              PdfFont fontBold) {
        if (itensJson == null || itensJson.isBlank() || "[]".equals(itensJson.trim())) {
            return null;
        }

        try {
            JsonElement root = JsonParser.parseString(itensJson);
            if (!root.isJsonArray() || root.getAsJsonArray().isEmpty()) {
                return null;
            }

            Table tabela = new Table(UnitValue.createPercentArray(new float[]{3f, 1.5f, 1.5f, 1.5f}));
            tabela.setWidth(UnitValue.createPercentValue(100));

            tabela.addHeaderCell(criarCabecalhoTabelaAuxiliar("Item", fontBold));
            tabela.addHeaderCell(criarCabecalhoTabelaAuxiliar("Quantidade", fontBold));
            tabela.addHeaderCell(criarCabecalhoTabelaAuxiliar("Medida", fontBold));
            tabela.addHeaderCell(criarCabecalhoTabelaAuxiliar("Valor", fontBold));

            JsonArray itens = root.getAsJsonArray();
            for (JsonElement el : itens) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject obj = el.getAsJsonObject();
                tabela.addCell(criarCelulaTabelaAuxiliar(valorTexto(obj, "item"), fontNormal, TextAlignment.LEFT));
                tabela.addCell(criarCelulaTabelaAuxiliar(valorTexto(obj, "quantidade"), fontNormal, TextAlignment.CENTER));
                tabela.addCell(criarCelulaTabelaAuxiliar(valorTexto(obj, "medida"), fontNormal, TextAlignment.CENTER));
                tabela.addCell(criarCelulaTabelaAuxiliar(valorTexto(obj, "valor"), fontNormal, TextAlignment.RIGHT));
            }
            return tabela;
        } catch (Exception e) {
            logger.warn("NÃ£o foi possÃ­vel montar tabela de itens de composiÃ§Ã£o: {}", e.getMessage());
            return null;
        }
    }

    private Cell criarCabecalhoTabelaAuxiliar(String texto, PdfFont fontBold) {
        return new Cell()
                .add(new Paragraph(texto).setFont(fontBold).setFontSize(FONT_BODY))
                .setBackgroundColor(DETAIL_AUX_HEADER_COLOR)
                .setPadding(CELL_PADDING);
    }

    private Cell criarCelulaTabelaAuxiliar(String texto, PdfFont fontNormal, TextAlignment alinhamento) {
        return new Cell()
                .add(new Paragraph(texto).setFont(fontNormal).setFontSize(FONT_BODY))
                .setTextAlignment(alinhamento)
                .setPadding(CELL_PADDING);
    }

    private String valorTexto(JsonObject obj, String key) {
        JsonElement value = obj.get(key);
        if (value == null || value.isJsonNull()) {
            return "";
        }
        return value.getAsString();
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    //  Event handler: CabeÃ§alho e RodapÃ©
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Handler de eventos de pÃ¡gina que desenha o cabeÃ§alho e o rodapÃ© em cada
     * pÃ¡gina do documento PDF.
     *
     * <p><b>CabeÃ§alho:</b></p>
     * <ul>
     *   <li>TÃ­tulo centralizado em negrito 14pt + "PÃ¡gina: N" alinhado Ã  direita</li>
     *   <li>Nomes das colunas em negrito 12pt, centralizados em cada coluna</li>
     *   <li>Linha separadora horizontal</li>
     * </ul>
     *
     * <p><b>RodapÃ©:</b></p>
     * <ul>
     *   <li>Linha separadora horizontal</li>
     *   <li>Data/hora alinhada Ã  esquerda (fuso America/Sao_Paulo)</li>
     *   <li>"Emitido pelo Ficha TÃ©cnica Ollivander" centralizado</li>
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
                // Novo content stream inserido ANTES do conteÃºdo da pÃ¡gina
                PdfCanvas canvas = new PdfCanvas(
                        page.newContentStreamBefore(),
                        page.getResources(),
                        pdf
                );
                canvas.saveState();

                // â”€â”€ CABEÃ‡ALHO â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

                // y base do cabeÃ§alho (imediatamente abaixo da margem superior da folha)
                float headerTop  = pageSize.getTop() - MARGIN_HORIZ;
                float titleY     = headerTop - FONT_TITLE - 4f;

                // TÃ­tulo â€“ centralizado, negrito 14pt
                float titleTextWidth = fontBold.getWidth(titulo, FONT_TITLE);
                float titleX         = leftX + (contentWidth - titleTextWidth) / 2f;
                canvas.beginText()
                        .setFontAndSize(fontBold, FONT_TITLE)
                        .moveText(titleX, titleY)
                        .showText(titulo)
                        .endText();

                // "PÃ¡gina: N" â€“ alinhado Ã  direita, normal 12pt
                String pageText      = "PÃ¡gina: " + pageNumber;
                float  pageTextWidth = fontNormal.getWidth(pageText, FONT_BODY);
                canvas.beginText()
                        .setFontAndSize(fontNormal, FONT_BODY)
                        .moveText(rightX - pageTextWidth, titleY)
                        .showText(pageText)
                        .endText();

                if (!columnLabels.isEmpty()) {
                    // CabeÃ§alhos das colunas â€“ negrito 12pt, centralizados em cada coluna
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

                    // Linha separadora abaixo dos cabeÃ§alhos das colunas
                    float headerLineY = colHeaderY - 8f;
                    canvas.setLineWidth(0.5f)
                            .moveTo(leftX,  headerLineY)
                            .lineTo(rightX, headerLineY)
                            .stroke();
                }

                // â”€â”€ RODAPÃ‰ â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

                // Linha separadora do rodapÃ©
                float footerLineY = MARGIN_HORIZ + 30f;
                canvas.setLineWidth(0.5f)
                        .moveTo(leftX,  footerLineY)
                        .lineTo(rightX, footerLineY)
                        .stroke();

                // Textos do rodapÃ©
                float footerTextY = footerLineY - FONT_BODY - 4f;

                // Data/hora â€“ alinhada Ã  esquerda
                String dataAtual = formatarDataAtual();
                canvas.beginText()
                        .setFontAndSize(fontNormal, FONT_BODY)
                        .moveText(leftX, footerTextY)
                        .showText(dataAtual)
                        .endText();

                // "Emitido pelo..." â€“ centralizado
                String emitterText  = "Emitido pelo Ficha TÃ©cnica Ollivander";
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
                log.error("Erro ao desenhar cabeÃ§alho/rodapÃ© na pÃ¡gina {}", pageNumber, e);
            }
        }

        /**
         * Retorna a data/hora atual formatada com o dia da semana em portuguÃªs,
         * usando o fuso horÃ¡rio America/Sao_Paulo.
         * Exemplo: "Quinta-feira, 13/03/2026 14:30:00"
         */
        private String formatarDataAtual() {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
            return now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        }
    }
}


