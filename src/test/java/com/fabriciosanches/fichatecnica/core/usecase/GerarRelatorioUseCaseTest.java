package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.core.domain.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.core.domain.enums.TipoRelatorio;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GerarRelatorioUseCaseTest {

    private final GerarRelatorioUseCase useCase = new GerarRelatorioUseCase();

    @Test
    void gerarRelatorioPDF_DeveGerarPdfValido() throws IOException {
        String json = "[{\"nome\":\"Produto A\",\"valor\":\"10.00\"}]";
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");
        colunas.put("valor", "Valor");

        byte[] pdf = useCase.gerarRelatorioPDF(new RelatorioRequestDTO(
                json, "", "Lista", colunas,
                TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, true
        ));

        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        assertEquals('%', pdf[0]);
        assertEquals('P', pdf[1]);
        assertEquals('D', pdf[2]);
        assertEquals('F', pdf[3]);
    }

    @Test
    void gerarRelatorioPDF_DeveFalharQuandoSemRegistros() {
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.gerarRelatorioPDF(new RelatorioRequestDTO(
                        "[]", "", "Lista", colunas,
                        TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false
                )));

        assertEquals("Nenhum registro encontrado para gerar o relatÃ³rio.", ex.getMessage());
    }

    @Test
    void gerarRelatorioPDF_DeveCorrigirTextoMojibakeNoConteudo() throws IOException {
        String json = "[{\"nome\":\"Ficha TÃ©cnica\",\"valor\":\"10.00\"}]";
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "DescriÃ§Ã£o");
        colunas.put("valor", "Valor");

        byte[] pdf = useCase.gerarRelatorioPDF(new RelatorioRequestDTO(
                json, "", "RelatÃ³rio TÃ©cnico", colunas,
                TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, true
        ));

        String textoExtraido = extrairTexto(pdf);

        assertTrue(textoExtraido.contains("Relatório Técnico"));
        assertTrue(textoExtraido.contains("Descrição"));
        assertTrue(textoExtraido.contains("Ficha Técnica"));
        assertFalse(textoExtraido.contains("TÃ©cnica"));
        assertFalse(textoExtraido.contains("DescriÃ§Ã£o"));
    }

    private String extrairTexto(byte[] pdfBytes) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
             PdfDocument pdfDocument = new PdfDocument(reader)) {
            for (int pagina = 1; pagina <= pdfDocument.getNumberOfPages(); pagina++) {
                sb.append(PdfTextExtractor.getTextFromPage(
                        pdfDocument.getPage(pagina),
                        new SimpleTextExtractionStrategy()
                ));
            }
        }
        return sb.toString();
    }
}


