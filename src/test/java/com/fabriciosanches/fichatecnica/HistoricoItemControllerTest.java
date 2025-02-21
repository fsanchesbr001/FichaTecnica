package com.fabriciosanches.fichatecnica;

import com.fabriciosanches.fichatecnica.controller.HistoricoItemController;
import com.fabriciosanches.fichatecnica.domain.historicoItem.DadosHistoricoItem;
import com.fabriciosanches.fichatecnica.services.HistoricoItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class HistoricoItemControllerTest {

    @Mock
    private HistoricoItemService historicoItemService;

    @InjectMocks
    private HistoricoItemController historicoItemController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBuscarLista() {
        DadosHistoricoItem dadosHistoricoItem = new DadosHistoricoItem(1L, null, BigDecimal.TEN, LocalDate.now());
        when(historicoItemService.listar()).thenReturn(List.of(dadosHistoricoItem));

        ResponseEntity<List<DadosHistoricoItem>> response = historicoItemController.buscarLista();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        verify(historicoItemService, times(1)).listar();
    }

    @Test
    void testBuscarPorId() {
        DadosHistoricoItem dadosHistoricoItem = new DadosHistoricoItem(1L, null, BigDecimal.TEN, LocalDate.now());
        when(historicoItemService.buscarPorId(1L)).thenReturn(dadosHistoricoItem);

        ResponseEntity<DadosHistoricoItem> response = historicoItemController.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dadosHistoricoItem, response.getBody());
        verify(historicoItemService, times(1)).buscarPorId(1L);
    }

    @Test
    void testApagar() {
        doNothing().when(historicoItemService).deletarItem(1L);

        ResponseEntity<Void> response = historicoItemController.apagar(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(historicoItemService, times(1)).deletarItem(1L);
    }

    @Test
    void testApagarPorCodItem() {
        doNothing().when(historicoItemService).deletarPorCodigoItem(1L);

        ResponseEntity<Void> response = historicoItemController.apagarPorCodItem(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(historicoItemService, times(1)).deletarPorCodigoItem(1L);
    }

    @Test
    void testAtualizar() {
        DadosHistoricoItem dadosHistoricoItem = new DadosHistoricoItem(1L, null, BigDecimal.TEN, LocalDate.now());
        when(historicoItemService.atualizarHistoricoItem(eq(1L), any(DadosHistoricoItem.class))).thenReturn(dadosHistoricoItem);

        ResponseEntity<DadosHistoricoItem> response = historicoItemController.atualizar(1L, dadosHistoricoItem);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dadosHistoricoItem, response.getBody());
        verify(historicoItemService, times(1)).atualizarHistoricoItem(eq(1L), any(DadosHistoricoItem.class));
    }

    @Test
    void testCadastrar() {
        DadosHistoricoItem dadosHistoricoItem = new DadosHistoricoItem(1L, null, BigDecimal.TEN, LocalDate.now());
        when(historicoItemService.cadastrarItem(any(DadosHistoricoItem.class))).thenReturn(dadosHistoricoItem);

        ResponseEntity<DadosHistoricoItem> response = historicoItemController.cadastrar(dadosHistoricoItem);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dadosHistoricoItem, response.getBody());
        verify(historicoItemService, times(1)).cadastrarItem(any(DadosHistoricoItem.class));
    }
}
