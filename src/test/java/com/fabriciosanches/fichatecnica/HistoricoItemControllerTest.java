package com.fabriciosanches.fichatecnica;

import com.fabriciosanches.fichatecnica.controllers.HistoricoItemController;
import com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO;
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
        HistoricoItemDTO historicoItemDTO = new HistoricoItemDTO(1L, null, BigDecimal.TEN, LocalDate.now());
        when(historicoItemService.listar()).thenReturn(List.of(historicoItemDTO));

        ResponseEntity<List<HistoricoItemDTO>> response = historicoItemController.buscarLista();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        verify(historicoItemService, times(1)).listar();
    }

    @Test
    void testBuscarPorId() {
        HistoricoItemDTO historicoItemDTO = new HistoricoItemDTO(1L, null, BigDecimal.TEN, LocalDate.now());
        when(historicoItemService.buscarPorId(1L)).thenReturn(historicoItemDTO);

        ResponseEntity<HistoricoItemDTO> response = historicoItemController.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(historicoItemDTO, response.getBody());
        verify(historicoItemService, times(1)).buscarPorId(1L);
    }
}
