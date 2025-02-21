package com.fabriciosanches.fichatecnica;

import com.fabriciosanches.fichatecnica.controller.ItemController;
import com.fabriciosanches.fichatecnica.domain.itens.DadosItem;
import com.fabriciosanches.fichatecnica.services.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListar() {
        DadosItem dadosItem = new DadosItem(1L, "Item1", null, BigDecimal.TEN);
        when(itemService.listar()).thenReturn(List.of(dadosItem));

        ResponseEntity<List<DadosItem>> response = itemController.buscarLista();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Item1", response.getBody().get(0).nome());
    }

    @Test
    void testBuscarPorId() {
        DadosItem dadosItem = new DadosItem(1L, "Item1", null, BigDecimal.TEN);
        when(itemService.buscarPorId(1L)).thenReturn(dadosItem);

        ResponseEntity<DadosItem> response = itemController.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Item1", response.getBody().nome());
    }

    @Test
    void testCadastrarItem() {
        DadosItem dadosItem = new DadosItem(1L, "Item1", null, BigDecimal.TEN);
        when(itemService.cadastrarItem(any(DadosItem.class))).thenReturn(dadosItem);

        ResponseEntity<DadosItem> response = itemController.cadastrarItem(dadosItem);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Item1", response.getBody().nome());
    }

    @Test
    void testAtualizarItem() {
        DadosItem dadosItem = new DadosItem(1L, "Item1", null, BigDecimal.TEN);
        when(itemService.atualizarItem(eq(1L), any(DadosItem.class))).thenReturn(dadosItem);

        ResponseEntity<DadosItem> response = itemController.atualizarItem(1L, dadosItem);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Item1", response.getBody().nome());
    }

    @Test
    void testDeletarItem() {
        doNothing().when(itemService).deletarItem(1L);

        ResponseEntity<Void> response = itemController.apagar(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        verify(itemService, times(1)).deletarItem(1L);
    }
}
