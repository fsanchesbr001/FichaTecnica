package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;

import java.io.IOException;

public interface GerarRelatorioPort {
    byte[] gerarRelatorioPDF(RelatorioRequestDTO request) throws IOException;
}

