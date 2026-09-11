package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPizzaProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarItensDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterDescricoesUnidadePort;
import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProdutoControllerTest {

    private MockMvc mockMvc;
    private BuscarProdutoPort buscarProdutoPort;
    private CriarProdutoPort criarProdutoPort;
    private AtualizarProdutoPort atualizarProdutoPort;
    private DeletarProdutoPort deletarProdutoPort;
    private ListarItensDoProdutoPort listarItensDoProdutoPort;
    private ObterDescricoesUnidadePort obterDescricoesUnidadePort;
    private GerarGraficoPizzaProdutoPort gerarGraficoPizzaProdutoPort;
    private GerarRelatorioPort gerarRelatorioPort;
    private GerarGraficoPort gerarGraficoPort;

    @BeforeEach
    void setUp() {
        buscarProdutoPort = Mockito.mock(BuscarProdutoPort.class);
        criarProdutoPort = Mockito.mock(CriarProdutoPort.class);
        atualizarProdutoPort = Mockito.mock(AtualizarProdutoPort.class);
        deletarProdutoPort = Mockito.mock(DeletarProdutoPort.class);
        listarItensDoProdutoPort = Mockito.mock(ListarItensDoProdutoPort.class);
        obterDescricoesUnidadePort = Mockito.mock(ObterDescricoesUnidadePort.class);
        gerarGraficoPizzaProdutoPort = Mockito.mock(GerarGraficoPizzaProdutoPort.class);
        gerarRelatorioPort = Mockito.mock(GerarRelatorioPort.class);
        gerarGraficoPort = Mockito.mock(GerarGraficoPort.class);

        ProdutoController controller = new ProdutoController(
                buscarProdutoPort,
                criarProdutoPort,
                atualizarProdutoPort,
                deletarProdutoPort,
                listarItensDoProdutoPort,
                obterDescricoesUnidadePort,
                gerarGraficoPizzaProdutoPort,
                gerarRelatorioPort,
                gerarGraficoPort
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void gerarPdfLista_DeveRetornarPdf() throws Exception {
        when(buscarProdutoPort.listar()).thenReturn(List.of(
                new ProdutoDTO(1L, "Bolo", "Chocolate", null, new BigDecimal("10.00"), new BigDecimal("7.00"))
        ));
        when(gerarRelatorioPort.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-lista").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }
}
