package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.controllers.ItemProdutoController;
import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaFatiaDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoCompletoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutosPorItemDTO;
import com.fabriciosanches.fichatecnica.dtos.QuantidadeValorDTO;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemProdutoControllerTest {

    private MockMvc mockMvc;
    private ItensProdutoService itensProdutoService;

    @BeforeEach
    void setUp() {
        itensProdutoService = Mockito.mock(ItensProdutoService.class);
        ItemProdutoController controller = new ItemProdutoController(itensProdutoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void salvarItemProduto_DeveRetornarOk() throws Exception {
        when(itensProdutoService.salvar(any(), any())).thenReturn(List.of(
                new ProdutoCompletoDTO("Bolo", "Farinha", 1L, 2.0, 1L, new BigDecimal("7.50"))
        ));

        mockMvc.perform(post("/ficha-tecnica/produtos/{idProduto}/itens", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {
                                    "cdItem": 1,
                                    "cdProduto": 1,
                                    "qtdItem": 2,
                                    "cdUnidadeMedida": 1,
                                    "vlrItem": 7.50
                                  }
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeProduto").value("Bolo"));
    }

    @Test
    void buscarItensProduto_DeveRetornarBadRequestQuandoFalhar() throws Exception {
        when(itensProdutoService.listarItensProduto(1L)).thenThrow(new RuntimeException("erro"));

        mockMvc.perform(get("/ficha-tecnica/produtos/{idProduto}/itens", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obterValoresItens_DeveRetornarOk() throws Exception {
        when(itensProdutoService.calcularQuantidadeEValorTotal(1L))
                .thenReturn(new QuantidadeValorDTO(2, new BigDecimal("15.00")));

        mockMvc.perform(get("/ficha-tecnica/produtos/{idProduto}/valores", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeTotal").value(2));
    }

    @Test
    void listarProdutosPorItem_DeveRetornarOk() throws Exception {
        when(itensProdutoService.listarProdutosPorItem(1L))
                .thenReturn(List.of(new ProdutosPorItemDTO(10L, "Bolo")));

        mockMvc.perform(get("/ficha-tecnica/itens/{idItem}/produtos", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProduto").value(10));
    }

    @Test
    void deletarItemProduto_DeveRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/ficha-tecnica/produtos/{idProduto}/itens/{idItem}", 1L, 2L))
                .andExpect(status().isNoContent());
    }

    @Test
    void atualizarQuantidadeItemProduto_DeveRetornarOk() throws Exception {
        mockMvc.perform(put("/ficha-tecnica/{idProduto}/{idItem}/quantidade", 1L, 2L)
                        .param("novaQuantidade", "3.5"))
                .andExpect(status().isOk());
    }

    @Test
    void gerarGraficoPizza_DeveRetornarOk() throws Exception {
        GraficoPizzaDTO dto = new GraficoPizzaDTO(
                "Bolo",
                "R$ 15,00",
                List.of(new GraficoPizzaFatiaDTO("Farinha", 1L, 50.0, "50,0%", "R$ 7,50",
                        new BigDecimal("7.50"), "R$ 15,00", "#FF6384")),
                List.of("Farinha"),
                List.of(50.0),
                List.of("#FF6384")
        );
        when(itensProdutoService.gerarGraficoPizza(1L)).thenReturn(dto);

        mockMvc.perform(get("/ficha-tecnica/produtos/{idProduto}/grafico-pizza", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeProduto").value("Bolo"));
    }

    @Test
    void gerarGraficoPizza_DeveRetornarNotFoundQuandoRegraNegocioFalhar() throws Exception {
        when(itensProdutoService.gerarGraficoPizza(1L)).thenThrow(new FichaTecnicaException("sem dados"));

        mockMvc.perform(get("/ficha-tecnica/produtos/{idProduto}/grafico-pizza", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void gerarGraficoPizza_DeveRetornarInternalServerErrorQuandoErroInesperado() throws Exception {
        when(itensProdutoService.gerarGraficoPizza(1L)).thenThrow(new RuntimeException("erro"));

        mockMvc.perform(get("/ficha-tecnica/produtos/{idProduto}/grafico-pizza", 1L))
                .andExpect(status().isInternalServerError());
    }
}

