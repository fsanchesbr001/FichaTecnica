package com.fabriciosanches.fichatecnica;

import com.fabriciosanches.fichatecnica.domains.HistoricoItem;
import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO;
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

        HistoricoItem historicoItem = new HistoricoItem(1L, item.getCodigo(), BigDecimal.TEN, LocalDate.now());
        when(historicoItemRepository.findAll()).thenReturn(List.of(historicoItem));

        List<HistoricoItemDTO> result = historicoItemService.listar();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).idItem());
    }

    @Test
    void testBuscarPorId() {
        Item item = new Item();
        item.setCodigo(1L);
        item.setNome("Item1");
        item.setValor(BigDecimal.TEN);

        HistoricoItem historicoItem = new HistoricoItem(1L, item.getCodigo(), BigDecimal.TEN, LocalDate.now());
        when(historicoItemRepository.findAll()).thenReturn(List.of(historicoItem));

        HistoricoItemDTO result = historicoItemService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1, result.idItem());
    }
}
