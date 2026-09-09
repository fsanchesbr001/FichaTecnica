package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.HistoricoItem;
import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.ports.out.HistoricoItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UnidadeMedidaEntity;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarHistoricoItemUseCaseTest {

    @Mock
    private HistoricoItemRepositoryPort historicoItemRepositoryPort;

    @Mock
    private ItemRepositoryPort itemRepositoryPort;

    @InjectMocks
    private ListarHistoricoItemUseCase useCase;

    @Test
    void listarPorCodigoItemOrdenadoPorDataInicio_DeveLancarQuandoVazio() {
        when(historicoItemRepositoryPort.buscarPorCodigoItemOrdenadoPorDataInicio(10L)).thenReturn(List.of());

        FichaTecnicaException ex = assertThrows(
                FichaTecnicaException.class,
                () -> useCase.listarPorCodigoItemOrdenadoPorDataInicio(10L)
        );

        assertEquals("Nenhum histórico encontrado para o item codigo=10", ex.getMessage());
    }

    @Test
    void gerarGraficoPreco_DeveMontarSerieComOrdemDosEventos() {
        List<HistoricoItem> historico = List.of(
                new HistoricoItem(1L, 10L, new BigDecimal("90.00"), LocalDate.of(2026, 9, 5)),
                new HistoricoItem(2L, 10L, new BigDecimal("85.00"), LocalDate.of(2026, 9, 5)),
                new HistoricoItem(3L, 10L, new BigDecimal("100.00"), LocalDate.of(2026, 9, 5))
        );

        when(historicoItemRepositoryPort.buscarPorCodigoItemOrdenadoPorDataInicio(10L)).thenReturn(historico);
        when(itemRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(
                new Item(10L, "Farinha", new UnidadeMedidaEntity(1L, "Quilo", "kg"), new BigDecimal("100.00"))
        ));

        GraficoPrecoItemDTO dto = useCase.gerarGraficoPreco(10L);

        assertEquals(3, dto.labels().size());
        assertEquals("05/09/2026", dto.labels().get(0).substring(0, 10));
        assertEquals("05/09/2026", dto.labels().get(1).substring(0, 10));
        assertEquals("05/09/2026", dto.labels().get(2).substring(0, 10));
        assertEquals(List.of("[#1]", "[#2]", "[#3]"), dto.labels().stream().map(label -> label.substring(label.indexOf('['))).toList());
        assertEquals(List.of(new BigDecimal("90.00"), new BigDecimal("85.00"), new BigDecimal("100.00")), dto.valores());
        assertEquals("-5,6%", dto.variacoes().get(1));
        assertEquals("+17,7%", dto.variacoes().get(2));
    }
}

