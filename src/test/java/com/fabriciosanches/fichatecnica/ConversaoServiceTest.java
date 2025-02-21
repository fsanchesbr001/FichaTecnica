package com.fabriciosanches.fichatecnica;

import com.fabriciosanches.fichatecnica.domain.conversao.Conversao;
import com.fabriciosanches.fichatecnica.domain.conversao.DadosConversao;
import com.fabriciosanches.fichatecnica.repository.ConversaoRepository;
import com.fabriciosanches.fichatecnica.services.ConversaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConversaoServiceTest {

    @Mock
    private ConversaoRepository repository;

    @InjectMocks
    private ConversaoService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListar() {
        DadosConversao dadosConversao = new DadosConversao(1L, 1L, 2L, "MULTIPLICACAO", BigDecimal.TEN);
        when(repository.findAll()).thenReturn(List.of(new Conversao(dadosConversao)));

        List<DadosConversao> result = service.listar();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).codigo());
    }

    @Test
    void testBuscarPorId() {
        DadosConversao dadosConversao = new DadosConversao(1L, 1L, 2L, "MULTIPLICACAO", BigDecimal.TEN);
        when(repository.findAll()).thenReturn(List.of(new Conversao(dadosConversao)));

        DadosConversao result = service.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.codigo());
    }

    @Test
    void testCadastrarConversao() {
        DadosConversao dadosConversao = new DadosConversao(1L, 1L, 2L, "MULTIPLICACAO", BigDecimal.TEN);
        Conversao conversao = new Conversao(dadosConversao);
        when(repository.save(any(Conversao.class))).thenReturn(conversao);

        DadosConversao result = service.cadastrarConversao(dadosConversao);

        assertNotNull(result);
        assertEquals(1L, result.codigo());
    }

    @Test
    void testAtualizarConversao() {
        DadosConversao dadosConversao = new DadosConversao(1L, 1L, 2L, "MULTIPLICACAO", BigDecimal.TEN);
        Conversao conversao = new Conversao(dadosConversao);
        when(repository.findById(1L)).thenReturn(Optional.of(conversao));
        when(repository.save(any(Conversao.class))).thenReturn(conversao);

        DadosConversao novosDados = new DadosConversao(1L, 1L, 2L, "DIVISAO", BigDecimal.ONE);
        DadosConversao result = service.atualizarConversao(1L, novosDados);

        assertNotNull(result);
        assertEquals("DIVISAO", result.operacao());
    }

    @Test
    void testDeletarConversao() {
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deletarConversao(1L));
        verify(repository, times(1)).deleteById(1L);
    }
}
