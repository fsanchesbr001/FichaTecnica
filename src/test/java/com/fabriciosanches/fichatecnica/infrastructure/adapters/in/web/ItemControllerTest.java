package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemControllerTest {

    private MockMvc mockMvc;
    private BuscarItemPort buscarItemPort;
    private CriarItemPort criarItemPort;
    private AtualizarItemPort atualizarItemPort;
    private DeletarItemPort deletarItemPort;
    private ListarHistoricoItemPort listarHistoricoItemPort;
    private GerarRelatorioPort gerarRelatorioPort;
    private GerarGraficoPort gerarGraficoPort;

    @BeforeEach
    void setUp() {
        buscarItemPort = Mockito.mock(BuscarItemPort.class);
        criarItemPort = Mockito.mock(CriarItemPort.class);
        atualizarItemPort = Mockito.mock(AtualizarItemPort.class);
        deletarItemPort = Mockito.mock(DeletarItemPort.class);
        listarHistoricoItemPort = Mockito.mock(ListarHistoricoItemPort.class);
        gerarRelatorioPort = Mockito.mock(GerarRelatorioPort.class);
        gerarGraficoPort = Mockito.mock(GerarGraficoPort.class);

        ItemController controller = new ItemController(
                buscarItemPort,
                criarItemPort,
                atualizarItemPort,
                deletarItemPort,
                listarHistoricoItemPort,
                gerarRelatorioPort,
                gerarGraficoPort
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscarLista_DeveRetornarOkQuandoHouverDados() throws Exception {
        when(buscarItemPort.listar()).thenReturn(List.of(
                new Item(1L, "Farinha", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("7.50"))
        ));

        mockMvc.perform(get("/ficha-tecnica/itens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1));
    }

    @Test
    void apagar_DeveRetornarUnprocessableEntityQuandoRegraNegocioFalhar() throws Exception {
        doThrow(new FichaTecnicaException("historico encontrado")).when(deletarItemPort).deletar(1L);

        mockMvc.perform(delete("/ficha-tecnica/itens/{id}", 1L))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atualizarItem_DeveRetornarOk() throws Exception {
        when(atualizarItemPort.atualizar(eq(1L), eq("Farinha Especial"), any(UnidadeMedida.class), any(BigDecimal.class)))
                .thenReturn(new Item(1L, "Farinha Especial", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("9.90")));

        mockMvc.perform(put("/ficha-tecnica/itens/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Farinha Especial",
                                  "unidadeMedida": {"codigo": 1, "nome": "Quilo", "sigla": "kg"},
                                  "valor": 9.90
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Farinha Especial"));
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarPdfComGrafico() throws Exception {
        when(buscarItemPort.buscarPorId(1L)).thenReturn(
                new Item(1L, "Farinha", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("7.50"))
        );
        when(listarHistoricoItemPort.gerarGraficoPreco(1L)).thenReturn(
                new GraficoPrecoItemDTO("Teste", "Item", List.of("01/01/2026"), List.of(new BigDecimal("7.50")),
                        List.of("R$ 7,50"), List.of("-"), List.of("-"))
        );
        when(gerarGraficoPort.gerarGraficoPNG(any())).thenReturn(new byte[]{1, 2, 3});
        when(gerarRelatorioPort.gerarRelatorioPDF(any())).thenReturn(new byte[]{4, 5, 6});

        mockMvc.perform(get("/ficha-tecnica/itens/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }
}

