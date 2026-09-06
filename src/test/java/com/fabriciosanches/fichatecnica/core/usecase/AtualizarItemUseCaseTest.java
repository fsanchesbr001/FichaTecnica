package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RegistrarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.domains.ItemProdutoId;
import com.fabriciosanches.fichatecnica.domains.Produto;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.ItemEntity;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UnidadeMedidaEntity;
import com.fabriciosanches.fichatecnica.repository.ItemProdutoRepository;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarItemUseCaseTest {

    @Mock
    private ItemRepositoryPort itemRepositoryPort;
    @Mock
    private RegistrarHistoricoItemPort registrarHistoricoItemPort;
    @Mock
    private ItemProdutoRepository itemProdutoRepository;
    @Mock
    private ObterValoresConversaoPort obterValoresConversaoPort;
    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private AtualizarItemUseCase useCase;

    @Test
    void atualizar_DeveLancarExcecaoQuandoItemNaoExiste() {
        UnidadeMedidaEntity unidade = new UnidadeMedidaEntity(1L, "Quilo", "kg");
        when(itemRepositoryPort.buscarPorId(1L)).thenReturn(Optional.empty());

        FichaTecnicaException ex = assertThrows(
                FichaTecnicaException.class,
                () -> useCase.atualizar(1L, "Farinha", unidade, new BigDecimal("10.00"))
        );

        assertEquals("Item com ID 1 não encontrado", ex.getMessage());
    }

    @Test
    void atualizar_DeveSalvarERegistrarHistoricoQuandoSemProdutosAssociados() {
        UnidadeMedidaEntity unidade = new UnidadeMedidaEntity(1L, "Quilo", "kg");
        Item item = new Item(1L, "Farinha", unidade, new BigDecimal("7.50"));

        when(itemRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(item));
        when(itemRepositoryPort.salvar(any(Item.class))).thenReturn(item);
        when(itemProdutoRepository.findByItemCodigo(1L)).thenReturn(List.of());

        Item result = useCase.atualizar(1L, "Farinha Premium", unidade, new BigDecimal("9.00"));

        assertEquals("Farinha Premium", result.getNome());
        verify(registrarHistoricoItemPort).registrar(eq(1L), eq(new BigDecimal("9.00")), any(LocalDate.class));
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void atualizar_DeveRecalcularProdutoQuandoExistemItensProduto() {
        UnidadeMedidaEntity unidadeBase = new UnidadeMedidaEntity(1L, "Quilo", "kg");
        UnidadeMedidaEntity unidadePara = new UnidadeMedidaEntity(2L, "Grama", "g");
        Item item = new Item(1L, "Farinha", unidadeBase, new BigDecimal("7.50"));

        Produto produto = new Produto(10L, "Bolo", "Desc", null, new BigDecimal("20.00"), BigDecimal.ZERO, null);
        ItemEntity itemEntity = new ItemEntity(1L, "Farinha", unidadeBase, new BigDecimal("7.50"));

        ItemProduto itemProduto = new ItemProduto(new ItemProdutoId(10L, 1L), itemEntity, produto, unidadePara,
                2.0, new BigDecimal("5.00"));
        produto.setProdutosList(List.of(itemProduto));

        when(itemRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(item));
        when(itemRepositoryPort.salvar(any(Item.class))).thenReturn(item);
        when(itemProdutoRepository.findByItemCodigo(1L)).thenReturn(List.of(itemProduto));
        when(obterValoresConversaoPort.obterValoresConversao(any(Item.class), eq(2.0), eq(2L)))
                .thenReturn(new ConversaoValoresDTO(2.0, 2L, new BigDecimal("7.00")));

        useCase.atualizar(1L, "Farinha Nova", unidadeBase, new BigDecimal("8.90"));

        assertEquals(new BigDecimal("7.00"), itemProduto.getValor());
        assertEquals(new BigDecimal("7.00"), produto.getValorItens());
        verify(itemProdutoRepository).save(itemProduto);
        verify(produtoRepository).save(produto);
    }
}

