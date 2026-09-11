package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;

import java.io.IOException;

public interface GerarGraficoPort {
    byte[] gerarGraficoPNG(GraficoPrecoItemDTO dto) throws IOException;

    byte[] gerarGraficoPizzaPNG(GraficoPizzaDTO dto) throws IOException;
}

