package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.ItemProdutoId;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemProdutoPersistenceAdapterTest {

    @Mock
    private SpringDataItemProdutoRepository repository;

    @InjectMocks
    private ItemProdutoPersistenceAdapter adapter;

    @Test
    void salvar_DeveMapearDominioParaEntidadeERetornarDominio() {
        ItemProduto domain = new ItemProduto(
                new ItemProdutoId(1L, 10L),
                new Item(10L, "Farinha", new UnidadeMedidaEntity(2L, "Quilo", "kg"), new BigDecimal("10.00")),
                new Produto(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO, List.of()),
                new UnidadeMedida(3L, "Grama", "g"),
                2.5,
                new BigDecimal("5.00"));

        when(repository.save(any(ItemProdutoEntity.class))).thenReturn(new ItemProdutoEntity(
                new ItemProdutoIdEntity(1L, 10L),
                new ItemEntity(10L, "Farinha", new UnidadeMedidaEntity(2L, "Quilo", "kg"), new BigDecimal("10.00")),
                new ProdutoEntity(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO, null),
                new UnidadeMedidaEntity(3L, "Grama", "g"),
                2.5,
                new BigDecimal("5.00")));

        ItemProduto salvo = adapter.salvar(domain);

        ArgumentCaptor<ItemProdutoEntity> captor = ArgumentCaptor.forClass(ItemProdutoEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(1L, captor.getValue().getId().getProdutoId());
        assertEquals(10L, salvo.getId().getItemId());
    }

    @Test
    void buscarPorProdutoIdEBuscarPorProdutoIdEItemId_DeveMapearEntidades() {
        ItemProdutoEntity entity = new ItemProdutoEntity(
                new ItemProdutoIdEntity(1L, 10L),
                new ItemEntity(10L, "Farinha", new UnidadeMedidaEntity(2L, "Quilo", "kg"), new BigDecimal("10.00")),
                new ProdutoEntity(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO, null),
                new UnidadeMedidaEntity(3L, "Grama", "g"),
                2.5,
                new BigDecimal("5.00"));

        when(repository.findByProdutoCodigo(1L)).thenReturn(List.of(entity));
        when(repository.findByProdutoCodigoAndItemCodigo(1L, 10L)).thenReturn(Optional.of(entity));

        List<ItemProduto> lista = adapter.buscarPorProdutoId(1L);
        ItemProduto porId = adapter.buscarPorProdutoIdEItemId(1L, 10L).orElseThrow();

        assertEquals(1, lista.size());
        assertEquals("Farinha", lista.get(0).getItem().getNome());
        assertEquals("Bolo", porId.getProduto().getNome());
    }

    @Test
    void deletarPorProdutoIdEItemId_DeveDelegarParaRepository() {
        adapter.deletarPorProdutoIdEItemId(1L, 10L);
        verify(repository).deleteByProdutoCodigoAndItemCodigo(1L, 10L);
    }
}

