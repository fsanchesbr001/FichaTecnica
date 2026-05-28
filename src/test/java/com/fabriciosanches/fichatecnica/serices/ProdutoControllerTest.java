package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.controllers.ProdutoController;
import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.ProdutoService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProdutoControllerTest {

    private MockMvc mockMvc;
    private ProdutoService produtoService;

    @BeforeEach
    void setUp() {
        produtoService = Mockito.mock(ProdutoService.class);
        ProdutoController controller = new ProdutoController(produtoService);
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
}

