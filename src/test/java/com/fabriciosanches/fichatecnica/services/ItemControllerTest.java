package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.controllers.ItemController;
import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
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

class ItemControllerTest {

    private MockMvc mockMvc;
    private ItemService itemService;
    private RelatorioService relatorioService;
    private HistoricoItemService historicoItemService;
    private GraficoService graficoService;

    @BeforeEach
    void setUp() {
        itemService = Mockito.mock(ItemService.class);
        relatorioService = Mockito.mock(RelatorioService.class);
        historicoItemService = Mockito.mock(HistoricoItemService.class);
        graficoService = Mockito.mock(GraficoService.class);

        ItemController controller = new ItemController(itemService, relatorioService, historicoItemService, graficoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscarLista_DeveRetornarOkQuandoHouverDados() throws Exception {
        when(itemService.listar()).thenReturn(List.of(new ItemDTO(1L, "Farinha", null, new BigDecimal("7.50"))));

        mockMvc.perform(get("/ficha-tecnica/itens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1));
    }

    @Test
    void buscarLista_DeveRetornarNoContentQuandoVazia() throws Exception {
        when(itemService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/ficha-tecnica/itens"))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscarPorId_DeveRetornarOkQuandoEncontrar() throws Exception {
        when(itemService.buscarPorId(1L)).thenReturn(new ItemDTO(1L, "Farinha", null, new BigDecimal("7.50")));

        mockMvc.perform(get("/ficha-tecnica/itens/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Farinha"));
    }

    @Test
    void buscarPorId_DeveRetornarNoContentQuandoNaoEncontrar() throws Exception {
        when(itemService.buscarPorId(1L)).thenReturn(null);

        mockMvc.perform(get("/ficha-tecnica/itens/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void apagar_DeveRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/ficha-tecnica/itens/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void apagar_DeveRetornarUnprocessableEntityQuandoExistirHistorico() throws Exception {
        doThrow(new FichaTecnicaException("historico encontrado")).when(itemService).deletarItem(1L);

        mockMvc.perform(delete("/ficha-tecnica/itens/{id}", 1L))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atualizarItem_DeveRetornarOk() throws Exception {
        when(itemService.atualizarItem(eq(1L), any(ItemDTO.class)))
                .thenReturn(new ItemDTO(1L, "Farinha Especial", null, new BigDecimal("9.90")));

        mockMvc.perform(put("/ficha-tecnica/itens/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Farinha Especial"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Farinha Especial"));
    }

    @Test
    void cadastrarItem_DeveRetornarBadRequestQuandoServicoFalhar() throws Exception {
        when(itemService.cadastrarItem(any(ItemDTO.class))).thenThrow(new FichaTecnicaException("erro"));

        mockMvc.perform(post("/ficha-tecnica/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Farinha"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gerarPdfLista_DeveRetornarNoContentQuandoNaoHouverItens() throws Exception {
        when(itemService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/ficha-tecnica/itens/gerar-pdf-lista"))
                .andExpect(status().isNoContent());
    }

    @Test
    void gerarPdfLista_DeveRetornarPdfQuandoGerarComSucesso() throws Exception {
        when(itemService.listar()).thenReturn(List.of(new ItemDTO(1L, "Farinha", null, new BigDecimal("7.50"))));
        when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/ficha-tecnica/itens/gerar-pdf-lista"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarOkQuandoGerarSemGrafico() throws Exception {
        when(itemService.buscarPorId(1L)).thenReturn(new ItemDTO(1L, "Farinha", null, new BigDecimal("7.50")));
        when(historicoItemService.gerarGraficoPreco(1L)).thenThrow(new FichaTecnicaException("sem historico"));
        when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{9, 8, 7});

        mockMvc.perform(get("/ficha-tecnica/itens/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarOkQuandoGerarComGrafico() throws Exception {
        when(itemService.buscarPorId(1L)).thenReturn(new ItemDTO(1L, "Farinha", null, new BigDecimal("7.50")));
        when(historicoItemService.gerarGraficoPreco(1L)).thenReturn(new GraficoPrecoItemDTO(
                "Variação de Preço - Farinha",
                "Farinha",
                List.of("10/01/2026"),
                List.of(new BigDecimal("7.50")),
                List.of("R$ 7,50"),
                List.of("-"),
                List.of("-")
        ));
        when(graficoService.gerarGraficoPNG(any())).thenReturn(new byte[]{5, 4, 3});
        when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{9, 8, 7});

        mockMvc.perform(get("/ficha-tecnica/itens/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarNotFoundQuandoItemNaoExistir() throws Exception {
        when(itemService.buscarPorId(1L)).thenThrow(new FichaTecnicaException("item nao encontrado"));

        mockMvc.perform(get("/ficha-tecnica/itens/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarBadRequestQuandoParametrosInvalidos() throws Exception {
        when(itemService.buscarPorId(1L)).thenReturn(new ItemDTO(1L, "Farinha", null, new BigDecimal("7.50")));
        when(relatorioService.gerarRelatorioPDF(any())).thenThrow(new IllegalArgumentException("dados invalidos"));

        mockMvc.perform(get("/ficha-tecnica/itens/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarInternalServerErrorQuandoErroInesperado() throws Exception {
        when(itemService.buscarPorId(1L)).thenReturn(new ItemDTO(1L, "Farinha", null, new BigDecimal("7.50")));
        when(relatorioService.gerarRelatorioPDF(any())).thenThrow(new RuntimeException("erro interno"));

        mockMvc.perform(get("/ficha-tecnica/itens/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isInternalServerError());
    }
}

