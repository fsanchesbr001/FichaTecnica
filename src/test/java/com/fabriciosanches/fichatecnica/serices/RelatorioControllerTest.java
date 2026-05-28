package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.controllers.RelatorioController;
import com.fabriciosanches.fichatecnica.services.RelatorioService;
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
    private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        relatorioService = Mockito.mock(RelatorioService.class);
        RelatorioController controller = new RelatorioController(relatorioService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void gerarPdf_DeveRetornarOkQuandoServicoGerarComSucesso() throws Exception {
        when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

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
        when(relatorioService.gerarRelatorioPDF(any())).thenThrow(new IllegalArgumentException("dados invalidos"));

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
        when(relatorioService.gerarRelatorioPDF(any())).thenThrow(new RuntimeException("erro interno"));

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

