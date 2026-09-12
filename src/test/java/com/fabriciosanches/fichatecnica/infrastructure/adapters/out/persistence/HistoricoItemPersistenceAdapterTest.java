package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.HistoricoItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoItemPersistenceAdapterTest {

    @Mock
    private SpringDataHistoricoItemRepository repository;

    @InjectMocks
    private HistoricoItemPersistenceAdapter adapter;

    @Test
    void salvar_DeveMapearDominioParaEntidadeERetornarDominio() {
        HistoricoItem domain = new HistoricoItem(1L, 10L, new BigDecimal("90.00"), LocalDate.of(2026, 9, 5));
        HistoricoItemEntity entity = new HistoricoItemEntity(1L, 10L, new BigDecimal("90.00"), LocalDate.of(2026, 9, 5));

        when(repository.save(any(HistoricoItemEntity.class))).thenReturn(entity);

        HistoricoItem salvo = adapter.salvar(domain);

        ArgumentCaptor<HistoricoItemEntity> captor = ArgumentCaptor.forClass(HistoricoItemEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(10L, captor.getValue().getCdItem());
        assertEquals(1L, salvo.getCodigo());
    }

    @Test
    void buscarPorCodigoItemOrdenadoPorDataInicio_DeveDelegarParaMetodoOrdenado() {
        when(repository.findByCdItemOrderByCdItemAscDataInicioAscCodigoAsc(10L)).thenReturn(List.of(
                new HistoricoItemEntity(1L, 10L, new BigDecimal("90.00"), LocalDate.of(2026, 9, 5)),
                new HistoricoItemEntity(2L, 10L, new BigDecimal("85.00"), LocalDate.of(2026, 9, 5))
        ));

        List<HistoricoItem> result = adapter.buscarPorCodigoItemOrdenadoPorDataInicio(10L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getCodigo());
        verify(repository).findByCdItemOrderByCdItemAscDataInicioAscCodigoAsc(10L);
    }

    @Test
    void buscarPorId_DeveMapearOptional() {
        when(repository.findById(5L)).thenReturn(Optional.of(
                new HistoricoItemEntity(5L, 10L, new BigDecimal("100.00"), LocalDate.of(2026, 9, 5))
        ));

        HistoricoItem result = adapter.buscarPorId(5L).orElseThrow();

        assertEquals(5L, result.getCodigo());
    }
}


