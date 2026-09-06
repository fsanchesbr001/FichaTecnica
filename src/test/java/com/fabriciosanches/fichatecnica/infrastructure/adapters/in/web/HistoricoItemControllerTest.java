package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.HistoricoItem;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
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
    private ListarHistoricoItemPort listarHistoricoItemPort;

    @BeforeEach
    void setUp() {
        listarHistoricoItemPort = Mockito.mock(ListarHistoricoItemPort.class);
        HistoricoItemController controller = new HistoricoItemController(listarHistoricoItemPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscarLista_DeveRetornarOk() throws Exception {
        when(listarHistoricoItemPort.listar())
                .thenReturn(List.of(new HistoricoItem(1L, 10L, new BigDecimal("7.50"), LocalDate.of(2026, 1, 10))));

        mockMvc.perform(get("/ficha-tecnica/historico-itens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1));
    }

    @Test
    void buscarPorId_DeveRetornarOk() throws Exception {
        when(listarHistoricoItemPort.buscarPorId(1L))
                .thenReturn(new HistoricoItem(1L, 10L, new BigDecimal("7.50"), LocalDate.of(2026, 1, 10)));

        mockMvc.perform(get("/ficha-tecnica/historico-itens/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idItem").value(10));
    }

    @Test
    void gerarGraficoPrecos_DeveRetornarNotFoundQuandoRegraNegocioFalhar() throws Exception {
        when(listarHistoricoItemPort.gerarGraficoPreco(10L)).thenThrow(new FichaTecnicaException("sem historico"));

        mockMvc.perform(get("/ficha-tecnica/historico-itens/grafico-precos/{codigoItem}", 10L))
                .andExpect(status().isNotFound());
    }

    @Test
    void gerarGraficoPrecos_DeveRetornarOk() throws Exception {
        GraficoPrecoItemDTO dto = new GraficoPrecoItemDTO(
                "Variacao de Preco - Farinha",
                "Farinha",
                List.of("10/01/2026 [#1]"),
                List.of(new BigDecimal("7.50")),
                List.of("R$ 7,50"),
                List.of("-"),
                List.of("-")
        );
        when(listarHistoricoItemPort.gerarGraficoPreco(10L)).thenReturn(dto);

        mockMvc.perform(get("/ficha-tecnica/historico-itens/grafico-precos/{codigoItem}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeItem").value("Farinha"));
    }
}

