package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.ItemProdutoId;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.AdicionarItemAoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarQuantidadeItemDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CalcularValoresItensProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPizzaProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarItensDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarProdutosPorItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RemoverItemDoProdutoPort;
import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaFatiaDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoCompletoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutosPorItemDTO;
import com.fabriciosanches.fichatecnica.dtos.QuantidadeValorDTO;
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
    private AdicionarItemAoProdutoPort adicionarItemAoProdutoPort;
    private ListarItensDoProdutoPort listarItensDoProdutoPort;
    private CalcularValoresItensProdutoPort calcularValoresItensProdutoPort;
    private ListarProdutosPorItemPort listarProdutosPorItemPort;
    private RemoverItemDoProdutoPort removerItemDoProdutoPort;
    private AtualizarQuantidadeItemDoProdutoPort atualizarQuantidadeItemDoProdutoPort;
    private GerarGraficoPizzaProdutoPort gerarGraficoPizzaProdutoPort;

    @BeforeEach
    void setUp() {
        adicionarItemAoProdutoPort = Mockito.mock(AdicionarItemAoProdutoPort.class);
        listarItensDoProdutoPort = Mockito.mock(ListarItensDoProdutoPort.class);
        calcularValoresItensProdutoPort = Mockito.mock(CalcularValoresItensProdutoPort.class);
        listarProdutosPorItemPort = Mockito.mock(ListarProdutosPorItemPort.class);
        removerItemDoProdutoPort = Mockito.mock(RemoverItemDoProdutoPort.class);
        atualizarQuantidadeItemDoProdutoPort = Mockito.mock(AtualizarQuantidadeItemDoProdutoPort.class);
        gerarGraficoPizzaProdutoPort = Mockito.mock(GerarGraficoPizzaProdutoPort.class);
        ItemProdutoController controller = new ItemProdutoController(
                adicionarItemAoProdutoPort,
                listarItensDoProdutoPort,
                calcularValoresItensProdutoPort,
                listarProdutosPorItemPort,
                removerItemDoProdutoPort,
                atualizarQuantidadeItemDoProdutoPort,
                gerarGraficoPizzaProdutoPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void salvarItemProduto_DeveRetornarOk() throws Exception {
        Item item = new Item(1L, "Farinha", new com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UnidadeMedidaEntity(1L, "Quilo", "kg"), new BigDecimal("10.00"));
        Produto produto = new Produto(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO, List.of());
        ItemProduto itemProduto = new ItemProduto(new ItemProdutoId(1L, 1L), item, produto, new UnidadeMedida(1L, "Quilo", "kg"), 2.0, new BigDecimal("5.00"));
        when(adicionarItemAoProdutoPort.adicionar(any(), any())).thenReturn(List.of(itemProduto));

        mockMvc.perform(post("/ficha-tecnica/produtos/{idProduto}/itens", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{"cdItem":1,"cdProduto":1,"qtdItem":2,"cdUnidadeMedida":1,"vlrItem":5.00}]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeProduto").value("Bolo"));
    }

    @Test
    void buscarItensProduto_DeveRetornarOk() throws Exception {
        Item item = new Item(1L, "Farinha", new com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UnidadeMedidaEntity(1L, "Quilo", "kg"), new BigDecimal("10.00"));
        Produto produto = new Produto(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO, List.of());
        when(listarItensDoProdutoPort.listar(1L)).thenReturn(List.of(new ItemProduto(new ItemProdutoId(1L, 1L), item, produto, new UnidadeMedida(1L, "Quilo", "kg"), 2.0, new BigDecimal("5.00"))));

        mockMvc.perform(get("/ficha-tecnica/produtos/{idProduto}/itens", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeProduto").value("Bolo"));
    }

    @Test
    void obterValoresItens_DeveRetornarOk() throws Exception {
        when(calcularValoresItensProdutoPort.calcular(1L)).thenReturn(new QuantidadeValorDTO(2, new BigDecimal("15.00")));

        mockMvc.perform(get("/ficha-tecnica/produtos/{idProduto}/valores", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeTotal").value(2));
    }

    @Test
    void listarProdutosPorItem_DeveRetornarOk() throws Exception {
        when(listarProdutosPorItemPort.listarPorItem(1L)).thenReturn(List.of(new ProdutosPorItemDTO(10L, "Bolo")));

        mockMvc.perform(get("/ficha-tecnica/itens/{idItem}/produtos", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeProduto").value("Bolo"));
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
                List.of(new GraficoPizzaFatiaDTO("Farinha", 1L, 50.0, "50,0%", "R$ 7,50", new BigDecimal("7.50"), "R$ 15,00", "#FF6384")),
                List.of("Farinha"),
                List.of(50.0),
                List.of("#FF6384")
        );
        when(gerarGraficoPizzaProdutoPort.gerar(1L)).thenReturn(dto);

        mockMvc.perform(get("/ficha-tecnica/produtos/{idProduto}/grafico-pizza", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeProduto").value("Bolo"));
    }
}

