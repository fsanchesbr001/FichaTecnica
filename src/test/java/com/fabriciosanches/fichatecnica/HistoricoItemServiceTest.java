package com.fabriciosanches.fichatecnica;

import com.fabriciosanches.fichatecnica.domain.historicoItem.DadosHistoricoItem;
import com.fabriciosanches.fichatecnica.domain.historicoItem.HistoricoItem;
import com.fabriciosanches.fichatecnica.domain.itens.Item;
import com.fabriciosanches.fichatecnica.repository.HistoricoItemRepository;
import com.fabriciosanches.fichatecnica.services.HistoricoItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistoricoItemServiceTest {

    @Mock
    private HistoricoItemRepository historicoItemRepository;

    @InjectMocks
    private HistoricoItemService historicoItemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListar() {
        Item item = new Item();
        item.setCodigo(1L);
        item.setNome("Item1");
        item.setValor(BigDecimal.TEN);

        HistoricoItem historicoItem = new HistoricoItem(1L, item, BigDecimal.TEN, LocalDate.now());
        when(historicoItemRepository.findAll()).thenReturn(List.of(historicoItem));

        List<DadosHistoricoItem> result = historicoItemService.listar();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Item1", result.get(0).item().getNome());
    }

    @Test
    void testBuscarPorId() {
        Item item = new Item();
        item.setCodigo(1L);
        item.setNome("Item1");
        item.setValor(BigDecimal.TEN);

        HistoricoItem historicoItem = new HistoricoItem(1L, item, BigDecimal.TEN, LocalDate.now());
        when(historicoItemRepository.findAll()).thenReturn(List.of(historicoItem));

        DadosHistoricoItem result = historicoItemService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals("Item1", result.item().getNome());
    }

    @Test
    void testCadastrarItem() {
        Item item = new Item();
        item.setCodigo(1L);
        item.setNome("Item1");
        item.setValor(BigDecimal.TEN);

        DadosHistoricoItem dadosHistoricoItem = new DadosHistoricoItem(1L, item, BigDecimal.TEN, LocalDate.now());
        HistoricoItem historicoItem = new HistoricoItem(dadosHistoricoItem);
        when(historicoItemRepository.save(any(HistoricoItem.class))).thenReturn(historicoItem);

        DadosHistoricoItem result = historicoItemService.cadastrarItem(dadosHistoricoItem);

        assertNotNull(result);
        assertEquals("Item1", result.item().getNome());
    }

    @Test
    void testAtualizarHistoricoItem() {
        Item item = new Item();
        item.setCodigo(1L);
        item.setNome("Item1");
        item.setValor(BigDecimal.TEN);

        HistoricoItem historicoItem = new HistoricoItem(1L, item, BigDecimal.TEN, LocalDate.now());
        when(historicoItemRepository.findById(1L)).thenReturn(Optional.of(historicoItem));
        when(historicoItemRepository.save(any(HistoricoItem.class))).thenReturn(historicoItem);

        DadosHistoricoItem novosDados = new DadosHistoricoItem(1L, item, BigDecimal.ONE, LocalDate.now());
        DadosHistoricoItem result = historicoItemService.atualizarHistoricoItem(1L, novosDados);

        assertNotNull(result);
        assertEquals(BigDecimal.ONE, result.valor());
    }

    @Test
    void testDeletarItem() {
        doNothing().when(historicoItemRepository).deleteById(1L);

        assertDoesNotThrow(() -> historicoItemService.deletarItem(1L));
        verify(historicoItemRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletarPorCodigoItem() {
        doNothing().when(historicoItemRepository).deleteByItemCodigo(1L);

        assertDoesNotThrow(() -> historicoItemService.deletarPorCodigoItem(1L));
        verify(historicoItemRepository, times(1)).deleteByItemCodigo(1L);
    }
}
