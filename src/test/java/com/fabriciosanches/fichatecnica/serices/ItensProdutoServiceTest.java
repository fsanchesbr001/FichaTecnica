package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.domains.Produto;
import com.fabriciosanches.fichatecnica.domains.UnidadeMedida;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutosPorItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ItemProdutoRepository;
import com.fabriciosanches.fichatecnica.repository.ItemRepository;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import com.fabriciosanches.fichatecnica.repository.UnidadeMedidaRepository;
import com.fabriciosanches.fichatecnica.services.ConversaoService;
import com.fabriciosanches.fichatecnica.services.ItensProdutoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItensProdutoServiceTest {

    @Mock
    private ItemProdutoRepository itemProdutoRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UnidadeMedidaRepository unidadeMedidaRepository;
    @Mock
    private ConversaoService conversaoService;

    @InjectMocks
    private ItensProdutoService service;

    @Test
    void obterDescricoesUnidade_DeveRetornarMapaVazioQuandoListaForNulaOuVazia() {
        assertTrue(service.obterDescricoesUnidade(null).isEmpty());
        assertTrue(service.obterDescricoesUnidade(List.of()).isEmpty());
    }

    @Test
    void obterDescricoesUnidade_DeveMontarDescricaoComFallbacks() {
        UnidadeMedida u1 = new UnidadeMedida(1L, "Quilograma", "kg");
        UnidadeMedida u2 = new UnidadeMedida(2L, "Litro", null);
        UnidadeMedida u3 = new UnidadeMedida(3L, null, "ml");
        UnidadeMedida u4 = new UnidadeMedida(4L, null, null);

        when(unidadeMedidaRepository.findAllById(List.of(1L, 2L, 3L, 4L)))
                .thenReturn(List.of(u1, u2, u3, u4));

        Map<Long, String> descricoes = service.obterDescricoesUnidade(List.of(1L, 2L, 3L, 4L));

        assertEquals("Quilograma (kg)", descricoes.get(1L));
        assertEquals("Litro", descricoes.get(2L));
        assertEquals("ml", descricoes.get(3L));
        assertEquals("-", descricoes.get(4L));
    }

    @Test
    void listarProdutosPorItem_DeveLancarExcecaoQuandoItemNaoExistir() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        FichaTecnicaException ex = assertThrows(FichaTecnicaException.class,
                () -> service.listarProdutosPorItem(99L));

        assertEquals("Item não encontrado", ex.getMessage());
    }

    @Test
    void listarProdutosPorItem_DeveRetornarProdutosRelacionados() {
        Item item = new Item();
        item.setCodigo(10L);

        Produto p1 = new Produto();
        p1.setCodigo(1L);
        p1.setNome("Bolo");

        Produto p2 = new Produto();
        p2.setCodigo(2L);
        p2.setNome("Torta");

        ItemProduto ip1 = new ItemProduto();
        ip1.setItem(item);
        ip1.setProduto(p1);

        ItemProduto ip2 = new ItemProduto();
        ip2.setItem(item);
        ip2.setProduto(p2);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemProdutoRepository.findByItem(item)).thenReturn(List.of(ip1, ip2));

        List<ProdutosPorItemDTO> resultado = service.listarProdutosPorItem(10L);

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).idProduto());
        assertEquals("Bolo", resultado.get(0).nomeProduto());
        assertEquals(2L, resultado.get(1).idProduto());
        assertEquals("Torta", resultado.get(1).nomeProduto());
    }

    @Test
    void gerarGraficoPizza_DeveLancarExcecaoQuandoProdutoSemItens() {
        Produto produto = new Produto();
        produto.setCodigo(1L);
        produto.setNome("Bolo");

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(itemProdutoRepository.findByProdutoCodigo(1L)).thenReturn(List.of());

        FichaTecnicaException ex = assertThrows(FichaTecnicaException.class,
                () -> service.gerarGraficoPizza(1L));

        assertTrue(ex.getMessage().contains("Nenhum item encontrado"));
    }

    @Test
    void gerarGraficoPizza_DeveCalcularPercentuaisECriarDto() {
        Produto produto = new Produto();
        produto.setCodigo(1L);
        produto.setNome("Bolo");

        Item farinha = new Item();
        farinha.setCodigo(10L);
        farinha.setNome("Farinha");

        Item acucar = new Item();
        acucar.setCodigo(20L);
        acucar.setNome("Açúcar");

        ItemProduto ip1 = new ItemProduto();
        ip1.setProduto(produto);
        ip1.setItem(farinha);
        ip1.setValor(new BigDecimal("10.00"));

        ItemProduto ip2 = new ItemProduto();
        ip2.setProduto(produto);
        ip2.setItem(acucar);
        ip2.setValor(new BigDecimal("30.00"));

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(itemProdutoRepository.findByProdutoCodigo(1L)).thenReturn(List.of(ip1, ip2));

        GraficoPizzaDTO dto = service.gerarGraficoPizza(1L);

        assertEquals("Bolo", dto.nomeProduto());
        assertEquals(2, dto.fatias().size());
        assertEquals(List.of("Farinha", "Açúcar"), dto.labels());
        assertEquals(25.0, dto.valores().get(0));
        assertEquals(75.0, dto.valores().get(1));
        assertTrue(dto.valorTotal().contains("40"));
    }

    @Test
    void atualizarQuantidadeItemProduto_DeveLancarExcecaoQuandoItemProdutoNaoExistir() {
        when(itemProdutoRepository.findByProdutoCodigoAndItemCodigo(1L, 2L)).thenReturn(null);

        FichaTecnicaException ex = assertThrows(FichaTecnicaException.class,
                () -> service.atualizarQuantidadeItemProduto(1L, 2L, 3.0));

        assertEquals("ItemProduto não encontrado para o produto e item especificados", ex.getMessage());
    }

    @Test
    void atualizarQuantidadeItemProduto_DeveAtualizarItemEValorTotalDoProduto() {
        Produto produto = new Produto();
        produto.setCodigo(1L);

        Item item = new Item();
        item.setCodigo(2L);

        UnidadeMedida unidade = new UnidadeMedida();
        unidade.setCodigo(3L);

        ItemProduto existente = new ItemProduto();
        existente.setProduto(produto);
        existente.setItem(item);
        existente.setUnidadePara(unidade);
        existente.setQuantidade(1.0);
        existente.setValor(new BigDecimal("5.00"));

        ItemProduto paraSomatorio = new ItemProduto();
        paraSomatorio.setValor(new BigDecimal("12.50"));

        when(itemProdutoRepository.findByProdutoCodigoAndItemCodigo(1L, 2L)).thenReturn(existente);
        when(conversaoService.obterValoresConversao(item, 3.5, 3L))
                .thenReturn(new ConversaoValoresDTO(3.5, 3L, new BigDecimal("12.50")));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(itemProdutoRepository.findByProdutoCodigo(1L)).thenReturn(List.of(paraSomatorio));

        service.atualizarQuantidadeItemProduto(1L, 2L, 3.5);

        assertEquals(3.5, existente.getQuantidade());
        assertEquals(new BigDecimal("12.50"), existente.getValor());
        assertEquals(new BigDecimal("12.50"), produto.getValorItens());
        verify(itemProdutoRepository).save(existente);
        verify(produtoRepository).save(produto);
    }
}

