package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RelatorioControllerTest {

    private MockMvc mockMvc;
    private GerarRelatorioPort gerarRelatorioPort;

    @BeforeEach
    void setUp() {
        gerarRelatorioPort = Mockito.mock(GerarRelatorioPort.class);
        GerarGraficoPort gerarGraficoPort = Mockito.mock(GerarGraficoPort.class);
        RelatorioController controller = new RelatorioController(gerarRelatorioPort, gerarGraficoPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void gerarPdf_DeveRetornarOkQuandoServicoGerarComSucesso() throws Exception {
        when(gerarRelatorioPort.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(post("/ficha-tecnica/relatorios/gerar-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jsonData":"[{\\"nome\\":\\"A\\"}]",
                                  "listPath":"",
                                  "titulo":"Relatorio Teste",
                                  "colunas":{"nome":"Nome"},
                                  "tipoRelatorio":"LISTA",
                                  "orientacao":"RETRATO",
                                  "alternarCores":false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void gerarPdf_DeveRetornarBadRequestQuandoServicoLancarIllegalArgumentException() throws Exception {
        when(gerarRelatorioPort.gerarRelatorioPDF(any())).thenThrow(new IllegalArgumentException("dados invalidos"));

        mockMvc.perform(post("/ficha-tecnica/relatorios/gerar-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jsonData":"[{\\"nome\\":\\"A\\"}]",
                                  "listPath":"",
                                  "titulo":"Relatorio Teste",
                                  "colunas":{"nome":"Nome"}
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gerarPdf_DeveRetornarInternalServerErrorQuandoServicoLancarErroInesperado() throws Exception {
        when(gerarRelatorioPort.gerarRelatorioPDF(any())).thenThrow(new RuntimeException("erro interno"));

        mockMvc.perform(post("/ficha-tecnica/relatorios/gerar-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jsonData":"[{\\"nome\\":\\"A\\"}]",
                                  "listPath":"",
                                  "titulo":"Relatorio Teste",
                                  "colunas":{"nome":"Nome"}
                                }
                                """))
                .andExpect(status().isInternalServerError());
    }
}


