package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.dtos.ConversaoDTO;
import com.fabriciosanches.fichatecnica.domains.Conversao;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ConversaoRepository;
import com.fabriciosanches.fichatecnica.services.ConversaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversaoServiceTest {

    @Mock
    private ConversaoRepository repository;

    @InjectMocks
    private ConversaoService service;

    private Conversao conversao;
    private ConversaoDTO conversaoDTO;

    @BeforeEach
    void setUp() {
        conversaoDTO = new ConversaoDTO(1L,1L,2L,"DIVIDE",
                new BigDecimal(1000));
        conversao = new Conversao(conversaoDTO);

    }

    @Test
    void listar_DeveRetornarListaDeConversoes() {
        when(repository.findAll()).thenReturn(List.of(conversao));

        List<ConversaoDTO> result = service.listar();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).unidadeDe());
        verify(repository, times(1)).findAll();
    }

    @Test
    void buscarPorId_DeveRetornarConversaoQuandoEncontrada() {
        when(repository.findById(1L)).thenReturn(Optional.of(conversao));

        ConversaoDTO result = service.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.codigo());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoNaoEncontrada() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class, () -> service.buscarPorId(1L));

        assertEquals("Conversão com ID 1 não encontrada", exception.getMessage());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void cadastrarConversao_DeveSalvarNovaConversao() {
        when(repository.save(any(Conversao.class))).thenReturn(conversao);

        ConversaoDTO result = service.cadastrarConversao(conversaoDTO);

        assertNotNull(result);
        assertEquals(1L, result.codigo());
        verify(repository, times(1)).save(any(Conversao.class));
    }

    @Test
    void atualizarConversao_DeveAtualizarConversaoExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(conversao));
        when(repository.save(any(Conversao.class))).thenReturn(conversao);

        ConversaoDTO novosDados =  new ConversaoDTO(1L,2L,1L,"MULTIPLICA",
                new BigDecimal(1000));
        ConversaoDTO result = service.atualizarConversao(1L, novosDados);

        assertNotNull(result);
        assertEquals(2L, result.unidadeDe());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(Conversao.class));
    }



    @Test
    void deletarConversao_DeveDeletarConversao() {
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deletarConversao(1L));

        verify(repository, times(1)).deleteById(1L);
    }
}
