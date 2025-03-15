package com.fabriciosanches.fichatecnica;

import com.fabriciosanches.fichatecnica.controllers.ConversaoController;
import com.fabriciosanches.fichatecnica.dtos.ConversaoDTO;
import com.fabriciosanches.fichatecnica.services.ConversaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ConversaoControllerTest {

    @Mock
    private ConversaoService conversaoService;

    @InjectMocks
    private ConversaoController conversaoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBuscarLista() {
        ConversaoDTO conversaoResponseDTO = new ConversaoDTO(1L, 1L, 2L, "MULTIPLICACAO", BigDecimal.TEN);
        when(conversaoService.listar()).thenReturn(List.of(conversaoResponseDTO));

        ResponseEntity<List<ConversaoDTO>> response = conversaoController.buscarLista();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        verify(conversaoService, times(1)).listar();
    }

    @Test
    void testBuscarPorId() {
        ConversaoDTO conversaoDTO = new ConversaoDTO(1L, 1L, 2L, "MULTIPLICACAO", BigDecimal.TEN);
        when(conversaoService.buscarPorId(1L)).thenReturn(conversaoDTO);

        ResponseEntity<ConversaoDTO> response = conversaoController.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(conversaoDTO, response.getBody());
        verify(conversaoService, times(1)).buscarPorId(1L);
    }

    @Test
    void testApagar() {
        doNothing().when(conversaoService).deletarConversao(1L);

        ResponseEntity<Void> response = conversaoController.apagar(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(conversaoService, times(1)).deletarConversao(1L);
    }

    @Test
    void testAtualizarConversao() {
        ConversaoDTO conversaoRequestDTO = new ConversaoDTO( 1L,1L, 2L, "MULTIPLICACAO", BigDecimal.TEN);
        when(conversaoService.atualizarConversao(eq(1L), any(ConversaoDTO.class)))
                .thenReturn(new ConversaoDTO(1L, 1L, 2L, "MULTIPLICACAO", BigDecimal.TEN));

        ResponseEntity<ConversaoDTO> response = conversaoController.atualizarConversao(1L, conversaoRequestDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new ConversaoDTO(1L, 1L, 2L, "MULTIPLICACAO", BigDecimal.TEN), response.getBody());
        verify(conversaoService, times(1)).atualizarConversao(eq(1L), any(ConversaoDTO.class));
    }

    @Test
    void testCadastrarConversao() {
        ConversaoDTO conversaoRequestDTO = new ConversaoDTO( 1L,1L, 2L, "MULTIPLICACAO", BigDecimal.TEN);
        when(conversaoService.cadastrarConversao(any(ConversaoDTO.class)))
                .thenReturn(new ConversaoDTO(1L, 1L, 2L, "MULTIPLICACAO", BigDecimal.TEN));

        ResponseEntity<ConversaoDTO> response = conversaoController.cadastrarConversao(conversaoRequestDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new ConversaoDTO(1L, 1L, 2L, "MULTIPLICACAO", BigDecimal.TEN), response.getBody());
        verify(conversaoService, times(1)).cadastrarConversao(any(ConversaoDTO.class));
    }
}
