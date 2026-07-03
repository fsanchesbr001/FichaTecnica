package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.controllers.HistoricoItemController;
import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
import com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HistoricoItemControllerTest {

    private MockMvc mockMvc;
    private HistoricoItemService historicoItemService;

    @BeforeEach
    void setUp() {
        historicoItemService = Mockito.mock(HistoricoItemService.class);
        HistoricoItemController controller = new HistoricoItemController(historicoItemService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscarLista_DeveRetornarOk() throws Exception {
        when(historicoItemService.listar())
                .thenReturn(List.of(new HistoricoItemDTO(1L, 10L, new BigDecimal("7.50"), LocalDate.of(2026, 1, 10))));

        mockMvc.perform(get("/ficha-tecnica/historico-itens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1));
    }

    @Test
    void buscarLista_DeveRetornarNotFoundQuandoServicoFalhar() throws Exception {
        when(historicoItemService.listar()).thenThrow(new FichaTecnicaException("erro"));

        mockMvc.perform(get("/ficha-tecnica/historico-itens"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorId_DeveRetornarOk() throws Exception {
        when(historicoItemService.buscarPorId(1L))
                .thenReturn(new HistoricoItemDTO(1L, 10L, new BigDecimal("7.50"), LocalDate.of(2026, 1, 10)));

        mockMvc.perform(get("/ficha-tecnica/historico-itens/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idItem").value(10));
    }

    @Test
    void buscarPorItemId_DeveRetornarOk() throws Exception {
        when(historicoItemService.buscarPorCodigoItem(10L))
                .thenReturn(List.of(new HistoricoItemDTO(1L, 10L, new BigDecimal("7.50"), LocalDate.of(2026, 1, 10))));

        mockMvc.perform(get("/ficha-tecnica/historico-itens/itens/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idItem").value(10));
    }

    @Test
    void gerarGraficoPrecos_DeveRetornarOk() throws Exception {
        GraficoPrecoItemDTO dto = new GraficoPrecoItemDTO(
                "Variação de Preço – Farinha",
                "Farinha",
                List.of("10/01/2026"),
                List.of(new BigDecimal("7.50")),
                List.of("R$ 7,50"),
                List.of("—"),
                List.of("—")
        );
        when(historicoItemService.gerarGraficoPreco(10L)).thenReturn(dto);

        mockMvc.perform(get("/ficha-tecnica/historico-itens/grafico-precos/{codigoItem}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeItem").value("Farinha"));
    }

    @Test
    void gerarGraficoPrecos_DeveRetornarNotFoundQuandoRegraNegocioFalhar() throws Exception {
        when(historicoItemService.gerarGraficoPreco(10L)).thenThrow(new FichaTecnicaException("sem historico"));

        mockMvc.perform(get("/ficha-tecnica/historico-itens/grafico-precos/{codigoItem}", 10L))
                .andExpect(status().isNotFound());
    }

    @Test
    void gerarGraficoPrecos_DeveRetornarInternalErrorQuandoErroInesperado() throws Exception {
        when(historicoItemService.gerarGraficoPreco(10L)).thenThrow(new RuntimeException("erro interno"));

        mockMvc.perform(get("/ficha-tecnica/historico-itens/grafico-precos/{codigoItem}", 10L))
                .andExpect(status().isInternalServerError());
    }
}

