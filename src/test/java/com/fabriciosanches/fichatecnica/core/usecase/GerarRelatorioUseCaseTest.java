package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.core.domain.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.core.domain.enums.TipoRelatorio;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}


