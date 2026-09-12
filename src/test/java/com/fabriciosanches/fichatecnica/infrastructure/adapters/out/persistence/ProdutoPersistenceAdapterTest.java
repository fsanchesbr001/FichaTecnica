package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.Produto;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoPersistenceAdapterTest {

    @Mock
    private SpringDataProdutoRepository repository;

    @InjectMocks
    private ProdutoPersistenceAdapter adapter;

    @Test
    void salvar_DeveMapearDominioParaEntidadeERetornarDominio() {
        Produto domain = new Produto(1L, "Bolo", "Desc", "img.jpg", new BigDecimal("19.90"), new BigDecimal("0.00"), List.of());
        when(repository.save(any(ProdutoEntity.class))).thenReturn(new ProdutoEntity(1L, "Bolo", "Desc", "img.jpg", new BigDecimal("19.90"), new BigDecimal("0.00"), null));

        Produto salvo = adapter.salvar(domain);

        ArgumentCaptor<ProdutoEntity> captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repository).save(captor.capture());
        assertEquals("Bolo", captor.getValue().getNome());
        assertEquals(1L, salvo.getCodigo());
    }

    @Test
    void buscarTodos_EBuscarPorId_DeveMapearEntidadesParaDominio() {
        when(repository.findAll()).thenReturn(List.of(
                new ProdutoEntity(1L, "Bolo", "Desc", "img.jpg", new BigDecimal("19.90"), BigDecimal.ZERO, null),
                new ProdutoEntity(2L, "Torta", "Desc2", null, new BigDecimal("25.00"), BigDecimal.ZERO, null)
        ));
        when(repository.findById(2L)).thenReturn(Optional.of(
                new ProdutoEntity(2L, "Torta", "Desc2", null, new BigDecimal("25.00"), BigDecimal.ZERO, null)
        ));

        List<Produto> todos = adapter.buscarTodos();
        Produto porId = adapter.buscarPorId(2L).orElseThrow();

        assertEquals(2, todos.size());
        assertEquals("Torta", porId.getNome());
    }

    @Test
    void contarPorNome_EDeletarPorId_DeveDelegarParaRepository() {
        when(repository.countByName("Bolo")).thenReturn(2L);

        assertEquals(2L, adapter.contarPorNome("Bolo"));
        adapter.deletarPorId(10L);
        verify(repository).deleteById(10L);
        assertTrue(true);
    }
}


