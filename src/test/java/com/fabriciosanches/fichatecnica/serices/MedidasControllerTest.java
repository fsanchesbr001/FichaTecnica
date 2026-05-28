package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.controllers.MedidasController;
import com.fabriciosanches.fichatecnica.dtos.UnidadeMedidaDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.RelatorioService;
import com.fabriciosanches.fichatecnica.services.UnidadeMedidaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MedidasControllerTest {

    private MockMvc mockMvc;
    private UnidadeMedidaService unidadeService;
    private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        unidadeService = Mockito.mock(UnidadeMedidaService.class);
        relatorioService = Mockito.mock(RelatorioService.class);

        MedidasController controller = new MedidasController(unidadeService, relatorioService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscarLista_DeveRetornarOk() throws Exception {
        when(unidadeService.listar()).thenReturn(List.of(new UnidadeMedidaDTO(1L, "Quilograma", "kg")));

        mockMvc.perform(get("/ficha-tecnica/unidades-medida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Quilograma"));
    }

    @Test
    void buscarPorId_DeveRetornarNotFoundQuandoFalhar() throws Exception {
        when(unidadeService.buscarPorId(1L)).thenThrow(new FichaTecnicaException("nao encontrado"));

        mockMvc.perform(get("/ficha-tecnica/unidades-medida/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void apagar_DeveRetornarConflictComMensagem() throws Exception {
        doThrow(new FichaTecnicaException("unidade em uso")).when(unidadeService).deletarUnidade(1L);

        mockMvc.perform(delete("/ficha-tecnica/unidades-medida/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(content().string("unidade em uso"));
    }

    @Test
    void atualizarUnidade_DeveRetornarBadRequestComMensagem() throws Exception {
        when(unidadeService.atualizarUnidade(eq(1L), any(UnidadeMedidaDTO.class)))
                .thenThrow(new FichaTecnicaException("sigla invalida"));

        mockMvc.perform(put("/ficha-tecnica/unidades-medida/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Quilograma",
                                  "sigla": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("sigla invalida"));
    }

    @Test
    void cadastrarUnidade_DeveRetornarOk() throws Exception {
        when(unidadeService.cadastrarUnidade(any(UnidadeMedidaDTO.class)))
                .thenReturn(new UnidadeMedidaDTO(1L, "Quilograma", "kg"));

        mockMvc.perform(post("/ficha-tecnica/unidades-medida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Quilograma",
                                  "sigla": "kg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1));
    }

    @Test
    void gerarPdfLista_DeveRetornarNoContentQuandoListaVazia() throws Exception {
        when(unidadeService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/ficha-tecnica/unidades-medida/gerar-pdf-lista"))
                .andExpect(status().isNoContent());
    }

    @Test
    void gerarPdfLista_DeveRetornarPdfQuandoSucesso() throws Exception {
        when(unidadeService.listar()).thenReturn(List.of(new UnidadeMedidaDTO(1L, "Quilograma", "kg")));
        when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/ficha-tecnica/unidades-medida/gerar-pdf-lista"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarNotFoundQuandoUnidadeNaoExistir() throws Exception {
        when(unidadeService.buscarPorId(1L)).thenThrow(new FichaTecnicaException("nao encontrado"));

        mockMvc.perform(get("/ficha-tecnica/unidades-medida/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarBadRequestQuandoParametrosInvalidos() throws Exception {
        when(unidadeService.buscarPorId(1L)).thenReturn(new UnidadeMedidaDTO(1L, "Quilograma", "kg"));
        when(relatorioService.gerarRelatorioPDF(any())).thenThrow(new IllegalArgumentException("dados invalidos"));

        mockMvc.perform(get("/ficha-tecnica/unidades-medida/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isBadRequest());
    }
}

