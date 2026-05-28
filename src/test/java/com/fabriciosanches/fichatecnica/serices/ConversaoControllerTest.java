package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.controllers.ConversaoController;
import com.fabriciosanches.fichatecnica.dtos.ConversaoDTO;
import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.ConversaoService;
import com.fabriciosanches.fichatecnica.services.RelatorioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConversaoControllerTest {

    private MockMvc mockMvc;
    private ConversaoService conversaoService;
    private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        conversaoService = Mockito.mock(ConversaoService.class);
        relatorioService = Mockito.mock(RelatorioService.class);
        ConversaoController controller = new ConversaoController(conversaoService, relatorioService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscarLista_DeveRetornarNoContentQuandoListaVazia() throws Exception {
        when(conversaoService.listarParaPdf()).thenReturn(List.of());

        mockMvc.perform(get("/ficha-tecnica/conversoes"))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscarPorId_DeveRetornarNotFoundQuandoFalhar() throws Exception {
        when(conversaoService.buscarPorId(1L)).thenThrow(new FichaTecnicaException("nao encontrado"));

        mockMvc.perform(get("/ficha-tecnica/conversoes/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void apagar_DeveRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/ficha-tecnica/conversoes/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void apagar_DeveRetornarNotFoundQuandoFalhar() throws Exception {
        doThrow(new FichaTecnicaException("erro")).when(conversaoService).deletarConversao(1L);

        mockMvc.perform(delete("/ficha-tecnica/conversoes/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarConversao_DeveRetornarOk() throws Exception {
        when(conversaoService.atualizarConversao(eq(1L), any(ConversaoDTO.class)))
                .thenReturn(new ConversaoDTO(1L, 1L, 2L, "*", new BigDecimal("1000.00")));

        mockMvc.perform(put("/ficha-tecnica/conversoes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "unidadeDe": 1,
                                  "unidadePara": 2,
                                  "operacao": "*",
                                  "valor": 1000.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1));
    }

    @Test
    void cadastrarConversao_DeveRetornarBadRequestQuandoFalhar() throws Exception {
        when(conversaoService.cadastrarConversao(any(ConversaoDTO.class)))
                .thenThrow(new FichaTecnicaException("erro"));

        mockMvc.perform(post("/ficha-tecnica/conversoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "unidadeDe": 1,
                                  "unidadePara": 2,
                                  "operacao": "*",
                                  "valor": 1000.00
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gerarPdfLista_DeveRetornarPdf() throws Exception {
        when(conversaoService.listarParaPdf()).thenReturn(List.of(
                new ConversaoRelatorioDTO(1L, "kg", "g", "*", new BigDecimal("1000.00"))
        ));
        when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/ficha-tecnica/conversoes/gerar-pdf-lista"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarNotFoundQuandoNaoEncontrar() throws Exception {
        when(conversaoService.buscarPorIdParaPdf(1L)).thenThrow(new FichaTecnicaException("nao encontrado"));

        mockMvc.perform(get("/ficha-tecnica/conversoes/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarInternalServerErrorQuandoErroInesperado() throws Exception {
        when(conversaoService.buscarPorIdParaPdf(1L))
                .thenReturn(new ConversaoRelatorioDTO(1L, "kg", "g", "*", new BigDecimal("1000.00")));
        when(relatorioService.gerarRelatorioPDF(any())).thenThrow(new RuntimeException("erro"));

        mockMvc.perform(get("/ficha-tecnica/conversoes/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isInternalServerError());
    }
}

