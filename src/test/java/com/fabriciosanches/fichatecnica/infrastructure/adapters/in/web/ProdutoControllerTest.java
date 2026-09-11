package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.ItemProdutoId;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPizzaProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarItensDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterDescricoesUnidadePort;
import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaFatiaDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.enums.ImagemPosicao;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.GraficoService;
import com.fabriciosanches.fichatecnica.services.RelatorioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProdutoControllerTest {

    private MockMvc mockMvc;
    private ProdutoController controller;
    private BuscarProdutoPort buscarProdutoPort;
    private CriarProdutoPort criarProdutoPort;
    private AtualizarProdutoPort atualizarProdutoPort;
    private DeletarProdutoPort deletarProdutoPort;
    private ListarItensDoProdutoPort listarItensDoProdutoPort;
    private ObterDescricoesUnidadePort obterDescricoesUnidadePort;
    private GerarGraficoPizzaProdutoPort gerarGraficoPizzaProdutoPort;
    private RelatorioService relatorioService;
    private GraficoService graficoService;

    @BeforeEach
    void setUp() {
        buscarProdutoPort = Mockito.mock(BuscarProdutoPort.class);
        criarProdutoPort = Mockito.mock(CriarProdutoPort.class);
        atualizarProdutoPort = Mockito.mock(AtualizarProdutoPort.class);
        deletarProdutoPort = Mockito.mock(DeletarProdutoPort.class);
        listarItensDoProdutoPort = Mockito.mock(ListarItensDoProdutoPort.class);
        obterDescricoesUnidadePort = Mockito.mock(ObterDescricoesUnidadePort.class);
        gerarGraficoPizzaProdutoPort = Mockito.mock(GerarGraficoPizzaProdutoPort.class);
        relatorioService = Mockito.mock(RelatorioService.class);
        graficoService = Mockito.mock(GraficoService.class);
        controller = new ProdutoController(
                buscarProdutoPort,
                criarProdutoPort,
                atualizarProdutoPort,
                deletarProdutoPort,
                listarItensDoProdutoPort,
                obterDescricoesUnidadePort,
                gerarGraficoPizzaProdutoPort,
                relatorioService,
                graficoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscarLista_DeveRetornarOkQuandoHouverDados() throws Exception {
        when(buscarProdutoPort.listar()).thenReturn(List.of(new ProdutoDTO(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO)));

        mockMvc.perform(get("/ficha-tecnica/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1));
    }

    @Test
    void buscarLista_DeveRetornarNoContentQuandoListaVazia() throws Exception {
        when(buscarProdutoPort.listar()).thenReturn(List.of());

        mockMvc.perform(get("/ficha-tecnica/produtos"))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscarPorId_DeveRetornarOkQuandoEncontrar() throws Exception {
        when(buscarProdutoPort.buscarPorId(1L)).thenReturn(new ProdutoDTO(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO));

        mockMvc.perform(get("/ficha-tecnica/produtos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bolo"));
    }

    @Test
    void apagar_DeveRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/ficha-tecnica/produtos/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void cadastrarProduto_DeveRetornarOk() throws Exception {
        when(criarProdutoPort.cadastrarProduto(any(ProdutoDTO.class)))
                .thenReturn(new ProdutoDTO(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO));

        mockMvc.perform(post("/ficha-tecnica/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Bolo","descricao":"Desc","valorVenda":19.90}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1));
    }

    @Test
    void gerarPdfLista_DeveRetornarPdfQuandoSucesso() throws Exception {
        when(buscarProdutoPort.listar()).thenReturn(List.of(new ProdutoDTO(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO)));
        when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-lista"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarPdfQuandoSucesso() throws Exception {
        Path tempDir = Files.createTempDirectory("produto-pdf-test");
        Path imagePath = tempDir.resolve("produtos/1/foto.jpg");
        Files.createDirectories(imagePath.getParent());
        Files.write(imagePath, new byte[]{1, 2, 3, 4});
        ReflectionTestUtils.setField(controller, "storagePath", tempDir.toString());
        ReflectionTestUtils.setField(controller, "publicUrl", "http://localhost/uploads");

        Produto produto = new Produto(1L, "Bolo", "Desc", "http://localhost/uploads/produtos/1/foto.jpg", new BigDecimal("19.90"), new BigDecimal("7.50"), List.of());
        Item item = new Item(10L, "Farinha", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("10.00"));
        ItemProduto itemProduto = new ItemProduto(new ItemProdutoId(1L, 10L), item, produto, new UnidadeMedida(1L, "Quilo", "kg"), 2.0, new BigDecimal("5.00"));
        when(buscarProdutoPort.buscarPorId(1L)).thenReturn(new ProdutoDTO(1L, "Bolo", "Desc", "http://localhost/uploads/produtos/1/foto.jpg", new BigDecimal("19.90"), new BigDecimal("7.50")));
        when(listarItensDoProdutoPort.listar(1L)).thenReturn(List.of(itemProduto));
        when(obterDescricoesUnidadePort.obter(List.of(1L))).thenReturn(Map.of(1L, "Quilo (kg)"));
        when(gerarGraficoPizzaProdutoPort.gerar(1L)).thenReturn(new GraficoPizzaDTO("Bolo", "R$ 5,00", List.of(new GraficoPizzaFatiaDTO("Farinha", 10L, 100.0, "100,0%", "R$ 5,00", new BigDecimal("5.00"), "R$ 5,00", "#FF6384")), List.of("Farinha"), List.of(100.0), List.of("#FF6384")));
        when(graficoService.gerarGraficoPizzaPNG(any())).thenReturn(new byte[]{9, 8, 7});
        when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));

        ArgumentCaptor<RelatorioRequestDTO> captor = ArgumentCaptor.forClass(RelatorioRequestDTO.class);
        verify(relatorioService).gerarRelatorioPDF(captor.capture());
        RelatorioRequestDTO request = captor.getValue();
        org.junit.jupiter.api.Assertions.assertTrue(request.jsonData().contains("Farinha"));
        org.junit.jupiter.api.Assertions.assertTrue(Boolean.TRUE.equals(request.usarImagem()));
        org.junit.jupiter.api.Assertions.assertEquals(ImagemPosicao.INICIO, request.imagemPosicao());
    }

    @Test
    void gerarPdfDetalhe_DeveRetornarNotFoundQuandoProdutoNaoEncontrar() throws Exception {
        when(buscarProdutoPort.buscarPorId(1L)).thenThrow(new FichaTecnicaException("Produto não encontrado"));

        mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-detalhe/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarProduto_DeveRetornarNotFoundQuandoFalhar() throws Exception {
        when(atualizarProdutoPort.atualizarProduto(eq(1L), any(ProdutoDTO.class))).thenThrow(new FichaTecnicaException("erro"));

        mockMvc.perform(put("/ficha-tecnica/produtos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Bolo Atualizado","descricao":"Desc"}
                                """))
                .andExpect(status().isNotFound());
    }
}
