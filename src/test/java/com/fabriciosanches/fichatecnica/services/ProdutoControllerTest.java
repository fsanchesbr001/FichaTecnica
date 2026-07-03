package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.controllers.ProdutoController;
import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaFatiaDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoCompletoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
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
    private ProdutoService produtoService;
    private RelatorioService relatorioService;
    private ItensProdutoService itensProdutoService;
    private GraficoService graficoService;

    @BeforeEach
    void setUp() {
        produtoService = Mockito.mock(ProdutoService.class);
        relatorioService = Mockito.mock(RelatorioService.class);
        itensProdutoService = Mockito.mock(ItensProdutoService.class);
        graficoService = Mockito.mock(GraficoService.class);
        controller = new ProdutoController(produtoService, relatorioService, itensProdutoService, graficoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscarLista_DeveRetornarOkQuandoHouverDados() throws Exception {
        when(produtoService.listar()).thenReturn(List.of(
                new ProdutoDTO(1L, "Bolo", "Bolo de chocolate", null, new BigDecimal("19.90"), BigDecimal.ZERO)
        ));

        mockMvc.perform(get("/ficha-tecnica/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(1));
    }

    @Test
    void buscarLista_DeveRetornarNoContentQuandoListaVazia() throws Exception {
        when(produtoService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/ficha-tecnica/produtos"))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscarPorId_DeveRetornarOkQuandoEncontrar() throws Exception {
        when(produtoService.buscarPorId(1L))
                .thenReturn(new ProdutoDTO(1L, "Bolo", "Bolo de chocolate", null, new BigDecimal("19.90"), BigDecimal.ZERO));

        mockMvc.perform(get("/ficha-tecnica/produtos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bolo"));
    }

    @Test
    void buscarPorId_DeveRetornarNoContentQuandoNaoEncontrar() throws Exception {
        when(produtoService.buscarPorId(1L)).thenReturn(null);

        mockMvc.perform(get("/ficha-tecnica/produtos/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscarPorId_DeveRetornarNotFoundQuandoServicoFalhar() throws Exception {
        when(produtoService.buscarPorId(1L)).thenThrow(new FichaTecnicaException("erro"));

        mockMvc.perform(get("/ficha-tecnica/produtos/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void apagar_DeveRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/ficha-tecnica/produtos/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void apagar_DeveRetornarNotFoundQuandoServicoFalhar() throws Exception {
        doThrow(new FichaTecnicaException("erro")).when(produtoService).deletarProduto(1L);

        mockMvc.perform(delete("/ficha-tecnica/produtos/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarProduto_DeveRetornarOk() throws Exception {
        when(produtoService.atualizarProduto(eq(1L), any(ProdutoDTO.class)))
                .thenReturn(new ProdutoDTO(1L, "Bolo Atualizado", "Desc", null, new BigDecimal("21.90"), BigDecimal.ZERO));

        mockMvc.perform(put("/ficha-tecnica/produtos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Bolo Atualizado",
                                  "descricao": "Desc"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bolo Atualizado"));
    }

    @Test
    void atualizarProduto_DeveRetornarNotFoundQuandoServicoFalhar() throws Exception {
        when(produtoService.atualizarProduto(eq(1L), any(ProdutoDTO.class)))
                .thenThrow(new FichaTecnicaException("erro"));

        mockMvc.perform(put("/ficha-tecnica/produtos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Bolo Atualizado",
                                  "descricao": "Desc"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void cadastrarProduto_DeveRetornarOk() throws Exception {
        when(produtoService.cadastrarProduto(any(ProdutoDTO.class)))
                .thenReturn(new ProdutoDTO(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO));

        mockMvc.perform(post("/ficha-tecnica/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Bolo",
                                  "descricao": "Desc"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1));
    }

    @Test
    void cadastrarProduto_DeveRetornarBadRequestQuandoServicoFalhar() throws Exception {
        when(produtoService.cadastrarProduto(any(ProdutoDTO.class)))
                .thenThrow(new FichaTecnicaException("erro"));

        mockMvc.perform(post("/ficha-tecnica/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Bolo",
                                  "descricao": "Desc"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gerarPdfLista_DeveRetornarNoContentQuandoListaVazia() throws Exception {
        when(produtoService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-lista"))
                .andExpect(status().isNoContent());
    }

    @Test
    void gerarPdfLista_DeveRetornarPdfQuandoSucesso() throws Exception {
        when(produtoService.listar()).thenReturn(List.of(
                new ProdutoDTO(1L, "Bolo", "Bolo de chocolate", null, new BigDecimal("19.90"), BigDecimal.ZERO)
        ));
        when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-lista"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void gerarPdfLista_DeveRetornarBadRequestQuandoParametroInvalido() throws Exception {
        when(produtoService.listar()).thenReturn(List.of(
                new ProdutoDTO(1L, "Bolo", "Bolo de chocolate", null, new BigDecimal("19.90"), BigDecimal.ZERO)
        ));
        when(relatorioService.gerarRelatorioPDF(any())).thenThrow(new IllegalArgumentException("dados invalidos"));

        mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-lista"))
                .andExpect(status().isBadRequest());
    }

     @Test
     void gerarPdfLista_DeveRetornarInternalServerErrorQuandoFalhaInesperada() throws Exception {
         when(produtoService.listar()).thenReturn(List.of(
                 new ProdutoDTO(1L, "Bolo", "Bolo de chocolate", null, new BigDecimal("19.90"), BigDecimal.ZERO)
         ));
         when(relatorioService.gerarRelatorioPDF(any())).thenThrow(new RuntimeException("erro interno"));

         mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-lista"))
                 .andExpect(status().isInternalServerError());
     }

     @Test
     void gerarPdfDetalhe_DeveRetornarNotFoundQuandoProdutoNaoEncontrar() throws Exception {
         when(produtoService.buscarPorId(1L)).thenThrow(new FichaTecnicaException("Produto não encontrado"));

         mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-detalhe/{id}", 1L))
                 .andExpect(status().isNotFound());
     }

     @Test
     void gerarPdfDetalhe_DeveRetornarPdfQuandoSucesso() throws Exception {
         when(produtoService.buscarPorId(1L))
                 .thenReturn(new ProdutoDTO(1L, "Bolo", "Bolo de chocolate", null, new BigDecimal("19.90"), BigDecimal.ZERO));
         when(itensProdutoService.listarItensProduto(1L)).thenReturn(List.of());
         when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

         mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-detalhe/{id}", 1L))
                 .andExpect(status().isOk())
                 .andExpect(header().string("Content-Type", "application/pdf"));
     }

     @Test
     void gerarPdfDetalhe_DeveRetornarBadRequestQuandoParametroInvalido() throws Exception {
         when(produtoService.buscarPorId(1L))
                 .thenReturn(new ProdutoDTO(1L, "Bolo", "Bolo de chocolate", null, new BigDecimal("19.90"), BigDecimal.ZERO));
         when(itensProdutoService.listarItensProduto(1L)).thenReturn(List.of());
         when(relatorioService.gerarRelatorioPDF(any())).thenThrow(new IllegalArgumentException("dados invalidos"));

         mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-detalhe/{id}", 1L))
                 .andExpect(status().isBadRequest());
     }

     @Test
     void gerarPdfDetalhe_DeveRetornarInternalServerErrorQuandoFalhaInesperada() throws Exception {
         when(produtoService.buscarPorId(1L))
                 .thenReturn(new ProdutoDTO(1L, "Bolo", "Bolo de chocolate", null, new BigDecimal("19.90"), BigDecimal.ZERO));
         when(itensProdutoService.listarItensProduto(1L)).thenReturn(List.of());
         when(relatorioService.gerarRelatorioPDF(any())).thenThrow(new RuntimeException("erro interno"));

         mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-detalhe/{id}", 1L))
                 .andExpect(status().isInternalServerError());
     }

     @Test
     void gerarPdfDetalhe_DeveIncluirItensEGraficoQuandoDisponiveis() throws Exception {
         when(produtoService.buscarPorId(1L))
                 .thenReturn(new ProdutoDTO(1L, "Parmegiana", "Desc", null, new BigDecimal("85.00"), new BigDecimal("16.95")));
         when(itensProdutoService.listarItensProduto(1L)).thenReturn(List.of(
                 new ProdutoCompletoDTO("Parmegiana", "Batata", 10L, 100d, 1L, new BigDecimal("0.80")),
                 new ProdutoCompletoDTO("Parmegiana", "File Mignon", 11L, 200d, 1L, new BigDecimal("15.00"))
         ));
         when(itensProdutoService.gerarGraficoPizza(1L)).thenReturn(new GraficoPizzaDTO(
                 "Parmegiana",
                 "R$ 16,95",
                 List.of(new GraficoPizzaFatiaDTO("Batata", 10L, 4.72, "4,7%", "R$ 0,80", new BigDecimal("0.80"), "R$ 16,95", "#FF6384")),
                 List.of("Batata"),
                 List.of(4.72),
                 List.of("#FF6384")
         ));
         when(itensProdutoService.obterDescricoesUnidade(List.of(1L))).thenReturn(Map.of(1L, "Grama (g)"));
         when(graficoService.gerarGraficoPizzaPNG(any())).thenReturn(new byte[]{9, 8, 7});
         when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

         mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-detalhe/{id}", 1L))
                 .andExpect(status().isOk())
                 .andExpect(header().string("Content-Type", "application/pdf"));

         ArgumentCaptor<RelatorioRequestDTO> captor = ArgumentCaptor.forClass(RelatorioRequestDTO.class);
         verify(relatorioService).gerarRelatorioPDF(captor.capture());

         RelatorioRequestDTO request = captor.getValue();
         org.junit.jupiter.api.Assertions.assertTrue(request.jsonData().contains("Batata"));
         org.junit.jupiter.api.Assertions.assertTrue(request.jsonData().contains("itensComposicaoTabelaJson"));
         org.junit.jupiter.api.Assertions.assertTrue(request.jsonData().contains("Grama (g)"));
         org.junit.jupiter.api.Assertions.assertTrue(Boolean.TRUE.equals(request.usarImagem()));
         org.junit.jupiter.api.Assertions.assertNotNull(request.imagem());
     }

     @Test
     void gerarPdfDetalhe_DeveManterImagemProdutoEAdicionarGraficoNoFim() throws Exception {
         Path tempDir = Files.createTempDirectory("produto-pdf-test");
         Path imagePath = tempDir.resolve("produtos/1/foto.jpg");
         Files.createDirectories(imagePath.getParent());
         Files.write(imagePath, new byte[]{1, 2, 3, 4});

         ReflectionTestUtils.setField(controller, "storagePath", tempDir.toString());
         ReflectionTestUtils.setField(controller, "publicUrl", "http://localhost/uploads");

         when(produtoService.buscarPorId(1L))
                 .thenReturn(new ProdutoDTO(1L, "Parmegiana", "Desc", "http://localhost/uploads/produtos/1/foto.jpg", new BigDecimal("85.00"), new BigDecimal("16.95")));
         when(itensProdutoService.listarItensProduto(1L)).thenReturn(List.of(
                 new ProdutoCompletoDTO("Parmegiana", "Batata", 10L, 100d, 1L, new BigDecimal("0.80"))
         ));
         when(itensProdutoService.obterDescricoesUnidade(List.of(1L))).thenReturn(Map.of(1L, "Grama (g)"));
         when(itensProdutoService.gerarGraficoPizza(1L)).thenReturn(new GraficoPizzaDTO(
                 "Parmegiana",
                 "R$ 16,95",
                 List.of(new GraficoPizzaFatiaDTO("Batata", 10L, 4.72, "4,7%", "R$ 0,80", new BigDecimal("0.80"), "R$ 16,95", "#FF6384")),
                 List.of("Batata"),
                 List.of(4.72),
                 List.of("#FF6384")
         ));
         when(graficoService.gerarGraficoPizzaPNG(any())).thenReturn(new byte[]{9, 8, 7});
         when(relatorioService.gerarRelatorioPDF(any())).thenReturn(new byte[]{1, 2, 3});

         mockMvc.perform(get("/ficha-tecnica/produtos/gerar-pdf-detalhe/{id}", 1L))
                 .andExpect(status().isOk())
                 .andExpect(header().string("Content-Type", "application/pdf"));

         ArgumentCaptor<RelatorioRequestDTO> captor = ArgumentCaptor.forClass(RelatorioRequestDTO.class);
         verify(relatorioService).gerarRelatorioPDF(captor.capture());

         RelatorioRequestDTO request = captor.getValue();
         org.junit.jupiter.api.Assertions.assertNotNull(request.imagem());
         org.junit.jupiter.api.Assertions.assertEquals(com.fabriciosanches.fichatecnica.enums.ImagemPosicao.INICIO, request.imagemPosicao());
         org.junit.jupiter.api.Assertions.assertNotNull(request.imagemSecundaria());
         org.junit.jupiter.api.Assertions.assertEquals(com.fabriciosanches.fichatecnica.enums.ImagemPosicao.FIM, request.imagemSecundariaPosicao());
     }
 }

