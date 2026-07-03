package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.HistoricoItem;
import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
import com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UnidadeMedidaEntity;
import com.fabriciosanches.fichatecnica.repository.HistoricoItemRepository;
import com.fabriciosanches.fichatecnica.repository.ItemRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoItemServiceTest {

    @Mock
    private HistoricoItemRepository repository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private HistoricoItemService service;

    private HistoricoItem historico1;
    private HistoricoItem historico2;

    @BeforeEach
    void setUp() {
        historico1 = new HistoricoItem(1L, 10L, new BigDecimal("7.50"), LocalDate.of(2026, 1, 10));
        historico2 = new HistoricoItem(2L, 10L, new BigDecimal("8.00"), LocalDate.of(2026, 2, 10));
    }

    @Test
    void listar_DeveRetornarListaDeHistorico() {
        when(repository.findAll()).thenReturn(List.of(historico1, historico2));

        List<HistoricoItemDTO> result = service.listar();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).codigo());
    }

    @Test
    void buscarPorId_DeveRetornarHistoricoQuandoEncontrado() {
        when(repository.findAll()).thenReturn(List.of(historico1, historico2));

        HistoricoItemDTO result = service.buscarPorId(2L);

        assertEquals(2L, result.codigo());
        assertEquals(new BigDecimal("8.00"), result.valor());
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoNaoEncontrado() {
        when(repository.findAll()).thenReturn(List.of(historico1));

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.buscarPorId(99L));

        assertEquals("Historico de item não encontrado", exception.getMessage());
    }

    @Test
    void buscarPorCodigoItem_DeveRetornarListaFiltrada() {
        when(repository.findByCdItem(10L)).thenReturn(List.of(historico1, historico2));

        List<HistoricoItemDTO> result = service.buscarPorCodigoItem(10L);

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).idItem());
    }

    @Test
    void buscarPorItemOrdenado_DeveRetornarListaOrdenada() {
        when(repository.findByCdItemOrderByDataInicioAsc(10L)).thenReturn(List.of(historico1, historico2));

        List<HistoricoItemDTO> result = service.buscarPorItemOrdenado(10L);

        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2026, 1, 10), result.get(0).dataInicio());
    }

    @Test
    void buscarPorItemOrdenado_DeveLancarExcecaoQuandoNaoExistirHistorico() {
        when(repository.findByCdItemOrderByDataInicioAsc(10L)).thenReturn(List.of());

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.buscarPorItemOrdenado(10L));

        assertEquals("Nenhum histórico encontrado para o item codigo=10", exception.getMessage());
    }

    @Test
    void gerarGraficoPreco_DeveGerarVariacoesComNomeDoItem() {
        when(repository.findByCdItemOrderByDataInicioAsc(10L)).thenReturn(List.of(historico1, historico2));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(
                new Item(10L, "Farinha", new UnidadeMedidaEntity(1L, "Quilo", "kg"), null, new BigDecimal("8.00"))
        ));

        GraficoPrecoItemDTO result = service.gerarGraficoPreco(10L);

        assertEquals("Variação de Preço – Farinha", result.titulo());
        assertEquals("Farinha", result.nomeItem());
        assertEquals(List.of("10/01/2026", "10/02/2026"), result.labels());
        assertEquals(List.of("R$ 7,50", "R$ 8,00"), result.valoresFormatados());
        assertEquals("—", result.variacoes().get(0));
        assertEquals("+6,7%", result.variacoes().get(1));
        assertEquals("+R$ 0,50", result.variacoesMonetarias().get(1));
    }

    @Test
    void gerarGraficoPreco_DeveUsarNomeFallbackETratarValorAnteriorZero() {
        HistoricoItem zero = new HistoricoItem(1L, 10L, BigDecimal.ZERO, LocalDate.of(2026, 1, 1));
        HistoricoItem proximo = new HistoricoItem(2L, 10L, new BigDecimal("2.50"), LocalDate.of(2026, 1, 2));
        when(repository.findByCdItemOrderByDataInicioAsc(10L)).thenReturn(List.of(zero, proximo));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        GraficoPrecoItemDTO result = service.gerarGraficoPreco(10L);

        assertEquals("Item 10", result.nomeItem());
        assertEquals("—", result.variacoes().get(0));
        assertEquals("—", result.variacoes().get(1));
        assertEquals("—", result.variacoesMonetarias().get(1));
    }

    @Test
    void gerarGraficoPreco_DeveTratarValorAtualNulo() {
        HistoricoItem semValor = new HistoricoItem(2L, 10L, null, LocalDate.of(2026, 2, 10));
        when(repository.findByCdItemOrderByDataInicioAsc(10L)).thenReturn(List.of(historico1, semValor));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(
                new Item(10L, "Farinha", new UnidadeMedidaEntity(1L, "Quilo", "kg"), null, new BigDecimal("8.00"))
        ));

        GraficoPrecoItemDTO result = service.gerarGraficoPreco(10L);

        assertEquals("—", result.variacoes().get(1));
        assertEquals("—", result.variacoesMonetarias().get(1));
        assertEquals("—", result.valoresFormatados().get(1));
    }
}
