package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.ItemProdutoId;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutosPorItemDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.QuantidadeValorDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItensProdutoUseCaseTest {

    @Mock
    private ItemProdutoRepositoryPort itemProdutoRepositoryPort;
    @Mock
    private ProdutoRepositoryPort produtoRepositoryPort;
    @Mock
    private ItemRepositoryPort itemRepositoryPort;
    @Mock
    private UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort;
    @Mock
    private ObterValoresConversaoPort obterValoresConversaoPort;

    @InjectMocks
    private ItensProdutoUseCase useCase;

    @Test
    void obter_DeveRetornarMapaVazioParaListaNulaOuVazia() {
        assertTrue(useCase.obter(null).isEmpty());
        assertTrue(useCase.obter(List.of()).isEmpty());
    }

    @Test
    void obter_DeveMontarDescricaoDaUnidade() {
        when(unidadeMedidaRepositoryPort.buscarTodos()).thenReturn(List.of(
                new UnidadeMedida(1L, "Quilo", "kg"),
                new UnidadeMedida(2L, "Litro", "l")
        ));

        Map<Long, String> resultado = useCase.obter(List.of(1L, 2L));

        assertEquals("Quilo (kg)", resultado.get(1L));
        assertEquals("Litro (l)", resultado.get(2L));
    }

    @Test
    void listarPorItem_DeveLancarQuandoItemNaoExistir() {
        when(itemRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        FichaTecnicaException ex = org.junit.jupiter.api.Assertions.assertThrows(FichaTecnicaException.class,
                () -> useCase.listarPorItem(99L));

        assertEquals("Item não encontrado", ex.getMessage());
    }

    @Test
    void listarPorItem_DeveRetornarProdutosRelacionados() {
        Item item = new Item(10L, "Farinha", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("10.00"));
        Produto p1 = new Produto(1L, "Bolo", "Desc", null, new BigDecimal("20.00"), BigDecimal.ZERO, List.of());
        Produto p2 = new Produto(2L, "Torta", "Desc2", null, new BigDecimal("30.00"), BigDecimal.ZERO, List.of());
        ItemProduto ip1 = new ItemProduto(new ItemProdutoId(1L, 10L), item, p1, new UnidadeMedida(1L, "Quilo", "kg"), 2.0, new BigDecimal("5.00"));
        ItemProduto ip2 = new ItemProduto(new ItemProdutoId(2L, 10L), item, p2, new UnidadeMedida(1L, "Quilo", "kg"), 3.0, new BigDecimal("7.50"));

        when(itemRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(item));
        when(itemProdutoRepositoryPort.buscarPorItemId(10L)).thenReturn(List.of(ip1, ip2));

        List<ProdutosPorItemDTO> resultado = useCase.listarPorItem(10L);

        assertEquals(2, resultado.size());
        assertEquals("Bolo", resultado.get(0).nomeProduto());
    }

    @Test
    void adicionar_DeveSalvarItensEAtualizarValorDoProduto() {
        Item item = new Item(10L, "Farinha", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("10.00"));
        Produto produto = new Produto(1L, "Bolo", "Desc", null, new BigDecimal("20.00"), BigDecimal.ZERO, List.of());
        ItemProduto solicitado = new ItemProduto(new ItemProdutoId(1L, 10L), item, produto, new UnidadeMedida(1L, "Quilo", "kg"), 2.0, BigDecimal.ZERO);

        when(itemRepositoryPort.buscarTodos()).thenReturn(List.of(item));
        when(itemRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(item));
        when(unidadeMedidaRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(new UnidadeMedida(1L, "Quilo", "kg")));
        when(obterValoresConversaoPort.obterValoresConversao(any(Item.class), eq(2.0), eq(1L)))
                .thenReturn(new ConversaoValoresDTO(2.0, 1L, new BigDecimal("5.00")));
        when(itemProdutoRepositoryPort.salvar(any(ItemProduto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produto));
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemProdutoRepositoryPort.buscarPorProdutoId(1L)).thenReturn(List.of(
                new ItemProduto(new ItemProdutoId(1L, 10L), item, produto, new UnidadeMedida(1L, "Quilo", "kg"), 2.0, new BigDecimal("5.00"))));

        List<ItemProduto> resultado = useCase.adicionar(1L, List.of(solicitado));

        assertEquals(1, resultado.size());
        assertEquals(new BigDecimal("5.00"), resultado.get(0).getValor());
        verify(produtoRepositoryPort).salvar(any(Produto.class));
    }

    @Test
    void calcular_DeveSomarValoresDosItens() {
        Item item = new Item(10L, "Farinha", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("10.00"));
        Produto produto = new Produto(1L, "Bolo", "Desc", null, new BigDecimal("20.00"), BigDecimal.ZERO, List.of());
        when(itemProdutoRepositoryPort.buscarPorProdutoId(1L)).thenReturn(List.of(
                new ItemProduto(new ItemProdutoId(1L, 10L), item, produto, new UnidadeMedida(1L, "Quilo", "kg"), 2.0, new BigDecimal("5.00")),
                new ItemProduto(new ItemProdutoId(1L, 11L), item, produto, new UnidadeMedida(1L, "Quilo", "kg"), 1.0, new BigDecimal("7.50"))
        ));

        QuantidadeValorDTO resultado = useCase.calcular(1L);

        assertEquals(2, resultado.quantidadeTotal());
        assertEquals(new BigDecimal("12.50"), resultado.valorTotal());
    }

    @Test
    void gerar_DeveRetornarGraficoQuandoHouverItens() {
        Item item = new Item(10L, "Farinha", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("10.00"));
        Produto produto = new Produto(1L, "Bolo", "Desc", null, new BigDecimal("20.00"), BigDecimal.ZERO, List.of());
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produto));
        when(itemProdutoRepositoryPort.buscarPorProdutoId(1L)).thenReturn(List.of(
                new ItemProduto(new ItemProdutoId(1L, 10L), item, produto, new UnidadeMedida(1L, "Quilo", "kg"), 2.0, new BigDecimal("5.00"))
        ));

        GraficoPizzaDTO resultado = useCase.gerar(1L);

        assertEquals("Bolo", resultado.nomeProduto());
        assertEquals(1, resultado.fatias().size());
        assertEquals("Farinha", resultado.labels().get(0));
    }

    @Test
    void remover_DeveExcluirItemProdutoERecalcularValor() {
        Item item = new Item(10L, "Farinha", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("10.00"));
        Produto produto = new Produto(1L, "Bolo", "Desc", null, new BigDecimal("20.00"), BigDecimal.ZERO, List.of());
        ItemProduto existente = new ItemProduto(new ItemProdutoId(1L, 10L), item, produto, new UnidadeMedida(1L, "Quilo", "kg"), 2.0, new BigDecimal("5.00"));
        when(itemProdutoRepositoryPort.buscarPorProdutoIdEItemId(1L, 10L)).thenReturn(Optional.of(existente));
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produto));
        when(itemProdutoRepositoryPort.buscarPorProdutoId(1L)).thenReturn(List.of());
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.remover(1L, 10L);

        verify(itemProdutoRepositoryPort).deletarPorProdutoIdEItemId(1L, 10L);
        verify(produtoRepositoryPort).salvar(any(Produto.class));
    }

    @Test
    void atualizarQuantidade_DeveAtualizarQuantidadeEValor() {
        Item item = new Item(10L, "Farinha", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("10.00"));
        Produto produto = new Produto(1L, "Bolo", "Desc", null, new BigDecimal("20.00"), BigDecimal.ZERO, List.of());
        ItemProduto existente = new ItemProduto(new ItemProdutoId(1L, 10L), item, produto, new UnidadeMedida(1L, "Quilo", "kg"), 2.0, new BigDecimal("5.00"));
        when(itemProdutoRepositoryPort.buscarPorProdutoIdEItemId(1L, 10L)).thenReturn(Optional.of(existente));
        when(itemRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(item));
        when(obterValoresConversaoPort.obterValoresConversao(any(Item.class), eq(3.5), eq(1L)))
                .thenReturn(new ConversaoValoresDTO(3.5, 1L, new BigDecimal("12.50")));
        when(itemProdutoRepositoryPort.salvar(any(ItemProduto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produto));
        when(itemProdutoRepositoryPort.buscarPorProdutoId(1L)).thenReturn(List.of(
                new ItemProduto(new ItemProdutoId(1L, 10L), item, produto, new UnidadeMedida(1L, "Quilo", "kg"), 3.5, new BigDecimal("12.50"))
        ));
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.atualizarQuantidade(1L, 10L, 3.5);

        assertEquals(3.5, existente.getQuantidade());
        ArgumentCaptor<ItemProduto> captor = ArgumentCaptor.forClass(ItemProduto.class);
        verify(itemProdutoRepositoryPort).salvar(captor.capture());
        assertEquals(0, captor.getValue().getValor().compareTo(new BigDecimal("12.50")));
    }
}

