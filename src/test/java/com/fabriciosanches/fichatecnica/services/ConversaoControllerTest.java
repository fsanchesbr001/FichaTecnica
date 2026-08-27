package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioConversaoPort;
import com.fabriciosanches.fichatecnica.dtos.ConversaoDTO;
import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.ConversaoController;
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
    private BuscarConversaoPort buscarConversaoPort;
    private CriarConversaoPort criarConversaoPort;
    private AtualizarConversaoPort atualizarConversaoPort;
    private DeletarConversaoPort deletarConversaoPort;
    private GerarRelatorioConversaoPort gerarRelatorioConversaoPort;
    private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        buscarConversaoPort = Mockito.mock(BuscarConversaoPort.class);
        criarConversaoPort = Mockito.mock(CriarConversaoPort.class);
        atualizarConversaoPort = Mockito.mock(AtualizarConversaoPort.class);
        deletarConversaoPort = Mockito.mock(DeletarConversaoPort.class);
        gerarRelatorioConversaoPort = Mockito.mock(GerarRelatorioConversaoPort.class);
        relatorioService = Mockito.mock(RelatorioService.class);
        ConversaoController controller = new ConversaoController(
                buscarConversaoPort,
                criarConversaoPort,
                atualizarConversaoPort,
                deletarConversaoPort,
                gerarRelatorioConversaoPort,
                relatorioService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscarLista_DeveRetornarNoContentQuandoListaVazia() throws Exception {
        when(gerarRelatorioConversaoPort.buscarTodosComNomes()).thenReturn(List.of());

        mockMvc.perform(get("/ficha-tecnica/conversoes"))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscarPorId_DeveRetornarNotFoundQuandoFalhar() throws Exception {
        when(buscarConversaoPort.buscarPorId(1L)).thenThrow(new java.util.NoSuchElementException("nao encontrado"));

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
        doThrow(new IllegalArgumentException("erro")).when(deletarConversaoPort).deletar(1L);

        mockMvc.perform(delete("/ficha-tecnica/conversoes/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarConversao_DeveRetornarOk() throws Exception {
        when(atualizarConversaoPort.atualizar(eq(1L), any(Conversao.class)))
                .thenReturn(new Conversao(1L, 1L, 2L, "*", new BigDecimal("1000.00")));

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
        when(criarConversaoPort.criar(any(Conversao.class)))
                .thenThrow(new IllegalArgumentException("erro"));

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
        when(gerarRelatorioConversaoPort.buscarTodosComNomes()).thenReturn(List.of(
                new ConversaoRelatorioDTO(1L, "kg", "g", "*", new BigDecimal("1000.00"))
        ));
        when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/ficha-tecnica/conversoes/gerar-pdf-lista"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarNotFoundQuandoNaoEncontrar() throws Exception {
        when(gerarRelatorioConversaoPort.buscarPorIdComNomes(1L)).thenThrow(new java.util.NoSuchElementException("nao encontrado"));

        mockMvc.perform(get("/ficha-tecnica/conversoes/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarInternalServerErrorQuandoErroInesperado() throws Exception {
        when(gerarRelatorioConversaoPort.buscarPorIdComNomes(1L))
                .thenReturn(new ConversaoRelatorioDTO(1L, "kg", "g", "*", new BigDecimal("1000.00")));
        when(relatorioService.gerarRelatorioPDF(any())).thenThrow(new RuntimeException("erro"));

        mockMvc.perform(get("/ficha-tecnica/conversoes/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isInternalServerError());
    }
}
