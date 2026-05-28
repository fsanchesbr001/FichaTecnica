package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.domains.HistoricoItem;
import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.domains.ItemProdutoId;
import com.fabriciosanches.fichatecnica.domains.Produto;
import com.fabriciosanches.fichatecnica.domains.UnidadeMedida;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemDTO;
import com.fabriciosanches.fichatecnica.dtos.QuantidadeValorDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.HistoricoItemRepository;
import com.fabriciosanches.fichatecnica.repository.ItemProdutoRepository;
import com.fabriciosanches.fichatecnica.repository.ItemRepository;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import com.fabriciosanches.fichatecnica.services.ConversaoService;
import com.fabriciosanches.fichatecnica.services.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository repository;
    @Mock
    private HistoricoItemRepository historicoItemRepository;
    @Mock
    private ItemProdutoRepository itemProdutoRepository;
    @Mock
    private ConversaoService conversaoService;
    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ItemService service;

    private UnidadeMedida unidadeMedida;
    private UnidadeMedida unidadePara;
    private Item item;
    private ItemDTO itemDTO;

    @BeforeEach
    void setUp() {
        unidadeMedida = new UnidadeMedida(1L, "Quilo", "kg");
        unidadePara = new UnidadeMedida(2L, "Grama", "g");
        item = new Item(1L, "Farinha", unidadeMedida, null, new BigDecimal("7.50"));
        itemDTO = new ItemDTO(1L, "Farinha", unidadeMedida, new BigDecimal("7.50"));
    }

    @Test
    void listar_DeveRetornarItensOrdenados() {
        Item outroItem = new Item(2L, "Açúcar", unidadeMedida, null, new BigDecimal("4.00"));
        when(repository.findAll()).thenReturn(List.of(item, outroItem));

        List<ItemDTO> result = service.listar();

        assertEquals(2, result.size());
        assertEquals("Açúcar", result.get(0).nome());
        assertEquals("Farinha", result.get(1).nome());
    }

    @Test
    void buscarPorId_DeveRetornarItemQuandoEncontrado() {
        when(repository.findAll()).thenReturn(List.of(item));

        ItemDTO result = service.buscarPorId(1L);

        assertEquals(1L, result.codigo());
        assertEquals("Farinha", result.nome());
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoNaoEncontrado() {
        when(repository.findAll()).thenReturn(List.of(item));

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.buscarPorId(99L));

        assertEquals("Item com ID 99 não encontrado", exception.getMessage());
    }

    @Test
    void findByName_DeveDelegarParaRepository() {
        when(repository.countByName("Farinha")).thenReturn(2L);

        assertEquals(2L, service.findByName("Farinha"));
    }

    @Test
    void cadastrarItem_DeveSalvarItemEHistorico() {
        when(repository.countByName("Farinha")).thenReturn(0L);
        when(repository.save(any(Item.class))).thenReturn(item);

        ItemDTO result = service.cadastrarItem(itemDTO);

        assertEquals("Farinha", result.nome());
        verify(repository).save(any(Item.class));
        verify(historicoItemRepository).save(any(HistoricoItem.class));
    }

    @Test
    void cadastrarItem_DeveLancarExcecaoQuandoDuplicado() {
        when(repository.countByName("Farinha")).thenReturn(1L);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.cadastrarItem(itemDTO));

        assertEquals("Item já cadastrado", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void atualizarItem_DeveLancarExcecaoQuandoNaoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.atualizarItem(1L, itemDTO));

        assertEquals("Item com ID 1 não encontrado", exception.getMessage());
    }

    @Test
    void atualizarItem_DeveSalvarHistoricoMesmoSemProdutosAssociados() {
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(repository.save(any(Item.class))).thenReturn(item);
        when(itemProdutoRepository.findByItem(item)).thenReturn(List.of());

        ItemDTO novosDados = new ItemDTO(1L, "Farinha Especial", unidadeMedida, new BigDecimal("8.00"));

        ItemDTO result = service.atualizarItem(1L, novosDados);

        assertEquals("Farinha Especial", result.nome());
        assertEquals(new BigDecimal("8.00"), result.valor());
        verify(historicoItemRepository).save(any(HistoricoItem.class));
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void atualizarItem_DeveAtualizarProdutosAssociadosQuandoExistirem() {
        Produto produto = new Produto(10L, "Bolo", "Desc", null,
                new BigDecimal("20.00"), BigDecimal.ZERO, null);
        ItemProduto itemProduto = new ItemProduto(new ItemProdutoId(10L, 1L), item, produto, unidadePara,
                2.0, new BigDecimal("5.00"));
        produto.setProdutosList(List.of(itemProduto));

        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(repository.save(any(Item.class))).thenReturn(item);
        when(itemProdutoRepository.findByItem(item)).thenReturn(List.of(itemProduto));
        when(conversaoService.obterValoresConversao(item, 2.0, 2L))
                .thenReturn(new ConversaoValoresDTO(2.0, 2L, new BigDecimal("7.00")));

        ItemDTO novosDados = new ItemDTO(1L, "Farinha Premium", unidadeMedida, new BigDecimal("9.00"));

        ItemDTO result = service.atualizarItem(1L, novosDados);

        assertEquals("Farinha Premium", result.nome());
        assertEquals(new BigDecimal("7.00"), itemProduto.getValor());
        assertEquals(new BigDecimal("7.00"), produto.getValorItens());
        verify(itemProdutoRepository).save(itemProduto);
        verify(produtoRepository).save(produto);
    }

    @Test
    void deletarItem_DeveLancarExcecaoQuandoNaoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.deletarItem(1L));

        assertEquals("Item com ID 1 não encontrado", exception.getMessage());
    }

    @Test
    void deletarItem_DeveExcluirHistoricoRelacoesEAtualizarProduto() {
        Produto produto = new Produto(10L, "Bolo", "Desc", null,
                new BigDecimal("20.00"), new BigDecimal("15.00"), null);
        ItemProduto itemProduto = new ItemProduto(new ItemProdutoId(10L, 1L), item, produto, unidadePara,
                2.0, new BigDecimal("5.00"));
        produto.setProdutosList(List.of(itemProduto));

        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(historicoItemRepository.findByCdItem(1L)).thenReturn(List.of(
                new HistoricoItem(1L, 1L, new BigDecimal("7.50"), LocalDate.now()),
                new HistoricoItem(2L, 1L, new BigDecimal("8.00"), LocalDate.now())
        ));
        when(itemProdutoRepository.findByItem(item)).thenReturn(List.of(itemProduto));

        assertDoesNotThrow(() -> service.deletarItem(1L));

        assertEquals(new BigDecimal("10.00"), produto.getValorItens());
        verify(historicoItemRepository, times(2)).deleteHistoricoItemByCdItem(1L);
        verify(produtoRepository).save(produto);
        verify(itemProdutoRepository).deleteItemProduto(1L);
        verify(repository).deleteItem(1L);
    }

    @Test
    void calcularQuantidadeEValorTotal_DeveSomarValoresDaLista() {
        Produto produto = new Produto(10L, "Bolo", "Desc", null,
                new BigDecimal("20.00"), BigDecimal.ZERO, null);
        ItemProduto itemProduto1 = new ItemProduto(new ItemProdutoId(10L, 1L), item, produto, unidadePara,
                2.0, new BigDecimal("5.50"));
        ItemProduto itemProduto2 = new ItemProduto(new ItemProdutoId(10L, 2L),
                new Item(2L, "Leite", unidadeMedida, null, new BigDecimal("4.00")), produto, unidadePara,
                1.0, new BigDecimal("3.25"));
        produto.setProdutosList(List.of(itemProduto1, itemProduto2));

        QuantidadeValorDTO result = service.calcularQuantidadeEValorTotal(produto);

        assertEquals(2, result.quantidadeTotal());
        assertEquals(new BigDecimal("8.75"), result.valorTotal());
    }
}

