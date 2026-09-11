package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaFatiaDTO;
import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GerarGraficoUseCaseTest {

    private final GerarGraficoUseCase useCase = new GerarGraficoUseCase();

    @Test
    void gerarGraficoPNG_DeveGerarPngValido() throws IOException {
        GraficoPrecoItemDTO dto = new GraficoPrecoItemDTO(
                "Variação de Preço",
                "Farinha",
                List.of("10/01/2026", "10/02/2026"),
                List.of(new BigDecimal("7.50"), new BigDecimal("8.00")),
                List.of("R$ 7,50", "R$ 8,00"),
                List.of("-", "+6,7%"),
                List.of("-", "+R$ 0,50")
        );

        byte[] result = useCase.gerarGraficoPNG(dto);
        assertNotNull(result);
        assertTrue(result.length > 1000);
        assertEquals((byte) 0x89, result[0]);
        assertEquals((byte) 0x50, result[1]);
        assertEquals((byte) 0x4E, result[2]);
        assertEquals((byte) 0x47, result[3]);
    }

    @Test
    void gerarGraficoPizzaPNG_DeveGerarPngValido() throws IOException {
        GraficoPizzaDTO dto = new GraficoPizzaDTO(
                "Bolo",
                "R$ 15,00",
                List.of(
                        new GraficoPizzaFatiaDTO("Farinha", 1L, 66.67, "66,67%", "R$ 10,00", new BigDecimal("10.00"), "R$ 15,00", "#3366CC"),
                        new GraficoPizzaFatiaDTO("Açúcar", 2L, 33.33, "33,33%", "R$ 5,00", new BigDecimal("5.00"), "R$ 15,00", "#DC3912")
                ),
                List.of("Farinha", "Açúcar"),
                List.of(66.67, 33.33),
                List.of("#3366CC", "#DC3912")
        );

        byte[] result = useCase.gerarGraficoPizzaPNG(dto);
        assertNotNull(result);
        assertTrue(result.length > 1000);
    }
}

