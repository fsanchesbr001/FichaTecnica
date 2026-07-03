package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
import org.junit.jupiter.api.Test;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.IOException;
import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraficoServiceTest {

    private final GraficoService service = new GraficoService();

    @Test
    void gerarGraficoPNG_DeveGerarBytesPngValidos() throws IOException {
        GraficoPrecoItemDTO dto = new GraficoPrecoItemDTO(
                "Variação de Preço – Farinha",
                "Farinha",
                List.of("10/01/2026", "10/02/2026"),
                List.of(new BigDecimal("7.50"), new BigDecimal("8.00")),
                List.of("R$ 7,50", "R$ 8,00"),
                List.of("—", "+6,7%"),
                List.of("—", "+R$ 0,50")
        );

        byte[] result = service.gerarGraficoPNG(dto);

        assertNotNull(result);
        assertTrue(result.length > 1000);
        assertEquals((byte) 0x89, result[0]);
        assertEquals((byte) 0x50, result[1]);
        assertEquals((byte) 0x4E, result[2]);
        assertEquals((byte) 0x47, result[3]);
    }

    @Test
    void gerarGraficoPNG_DeveAceitarValoresNulosNoDataset() throws IOException {
        List<BigDecimal> valores = new ArrayList<>();
        valores.add(BigDecimal.ZERO);
        valores.add(null);

        GraficoPrecoItemDTO dto = new GraficoPrecoItemDTO(
                "Variação de Preço – Item 10",
                "Item 10",
                List.of("10/01/2026", "11/01/2026"),
                valores,
                List.of("R$ 0,00", "—"),
                List.of("—", "—"),
                List.of("—", "—")
        );

        byte[] result = service.gerarGraficoPNG(dto);

        assertNotNull(result);
        assertTrue(result.length > 1000);
    }

    @Test
    void tooltipSintetico_DeveMontarTextoEsperado() throws Exception {
        GraficoPrecoItemDTO dto = new GraficoPrecoItemDTO(
                "Variação de Preço – Farinha",
                "Farinha",
                List.of("10/01/2026", "10/02/2026"),
                List.of(new BigDecimal("7.50"), new BigDecimal("8.00")),
                List.of("R$ 7,50", "R$ 8,00"),
                List.of("—", "+6,7%"),
                List.of("—", "+R$ 0,50")
        );
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(new BigDecimal("7.50"), dto.nomeItem(), dto.labels().get(0));
        dataset.addValue(new BigDecimal("8.00"), dto.nomeItem(), dto.labels().get(1));

        Method method = GraficoService.class.getDeclaredMethod(
                "lambda$gerarGraficoPNG$0", List.class, com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO.class,
                org.jfree.data.category.CategoryDataset.class, int.class, int.class
        );
        method.setAccessible(true);

        String tooltip = (String) method.invoke(null, dto.labels(), dto, dataset, 0, 1);

        assertEquals("10/02/2026 – R$ 8,00 | +6,7% (+R$ 0,50)", tooltip);
    }
}

