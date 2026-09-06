package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.ports.out.HistoricoItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.domains.ItemProdutoId;
import com.fabriciosanches.fichatecnica.domains.Produto;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletarItemUseCaseTest {

    @Mock
    private ItemRepositoryPort itemRepositoryPort;
    @Mock
    private HistoricoItemRepositoryPort historicoItemRepositoryPort;
    @Mock
    private ItemProdutoRepository itemProdutoRepository;
    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private DeletarItemUseCase useCase;

    @Test
    void deletar_DeveLancarExcecaoQuandoItemNaoExiste() {
        when(itemRepositoryPort.buscarPorId(1L)).thenReturn(Optional.empty());

        FichaTecnicaException ex = assertThrows(FichaTecnicaException.class, () -> useCase.deletar(1L));

        assertEquals("Item com ID 1 não encontrado", ex.getMessage());
    }

    @Test
    void deletar_DeveExcluirHistoricoAtualizarProdutosERemoverItem() {
        UnidadeMedidaEntity unidade = new UnidadeMedidaEntity(1L, "Quilo", "kg");
        Item item = new Item(1L, "Farinha", unidade, new BigDecimal("8.00"));

        Produto produto = new Produto(10L, "Bolo", "Desc", null,
                new BigDecimal("20.00"), new BigDecimal("15.00"), null);
        ItemEntity itemEntity = new ItemEntity(1L, "Farinha", unidade, new BigDecimal("8.00"));
        ItemProduto itemProduto = new ItemProduto(new ItemProdutoId(10L, 1L), itemEntity, produto,
                unidade, 1.0, new BigDecimal("5.00"));

        when(itemRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(item));
        when(itemProdutoRepository.findByItemCodigo(1L)).thenReturn(List.of(itemProduto));

        useCase.deletar(1L);

        assertEquals(new BigDecimal("10.00"), produto.getValorItens());
        verify(historicoItemRepositoryPort).deletarPorCodigoItem(1L);
        verify(itemProdutoRepository).deleteItemProduto(1L);
        verify(produtoRepository).save(produto);
        verify(itemRepositoryPort).deletarPorId(1L);
    }
}

