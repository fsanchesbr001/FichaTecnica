package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.Item;
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
class ItemPersistenceAdapterTest {

    @Mock
    private SpringDataItemRepository repository;

    @InjectMocks
    private ItemPersistenceAdapter adapter;

    @Test
    void salvar_DeveMapearDominioParaEntidadeERetornarDominio() {
        UnidadeMedidaEntity unidade = new UnidadeMedidaEntity(1L, "Quilo", "kg");
        Item domain = new Item(1L, "Farinha", unidade, new BigDecimal("10.00"));

        when(repository.save(any(ItemEntity.class)))
                .thenReturn(new ItemEntity(1L, "Farinha", unidade, new BigDecimal("10.00")));

        Item salvo = adapter.salvar(domain);

        ArgumentCaptor<ItemEntity> captor = ArgumentCaptor.forClass(ItemEntity.class);
        verify(repository).save(captor.capture());
        assertEquals("Farinha", captor.getValue().getNome());
        assertEquals(1L, salvo.getCodigo());
    }

    @Test
    void buscarTodosEBuscarPorId_DeveMapearParaDominio() {
        UnidadeMedidaEntity unidade = new UnidadeMedidaEntity(1L, "Quilo", "kg");
        when(repository.findAll()).thenReturn(List.of(new ItemEntity(1L, "Farinha", unidade, new BigDecimal("10.00"))));
        when(repository.findById(1L)).thenReturn(Optional.of(new ItemEntity(1L, "Farinha", unidade, new BigDecimal("10.00"))));

        List<Item> todos = adapter.buscarTodos();
        Item porId = adapter.buscarPorId(1L).orElseThrow();

        assertEquals(1, todos.size());
        assertEquals("Farinha", porId.getNome());
    }
}

