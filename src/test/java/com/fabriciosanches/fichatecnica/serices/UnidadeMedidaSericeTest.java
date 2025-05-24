package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.dtos.UnidadeMedidaDTO;
import com.fabriciosanches.fichatecnica.domains.UnidadeMedida;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.UnidadeMedidaRepository;
import com.fabriciosanches.fichatecnica.services.UnidadeMedidaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnidadeMedidaServiceTest {

    @Mock
    private UnidadeMedidaRepository repository;

    @InjectMocks
    private UnidadeMedidaService service;

    private UnidadeMedida unidadeMedida;
    private UnidadeMedidaDTO unidadeMedidaDTO;


    @BeforeEach
    void setUp() {
        unidadeMedida = new UnidadeMedida(1L, "Metro", "m");
        unidadeMedidaDTO = new UnidadeMedidaDTO(1L, "Metro", "m");


    }

    @Test
    void listar_DeveRetornarListaOrdenada() {
        when(repository.findAll()).thenReturn(List.of(unidadeMedida));

        List<UnidadeMedidaDTO> result = service.listar();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Metro", result.get(0).nome());
        verify(repository, times(1)).findAll();
    }


    @Test
    void buscarPorId_DeveRetornarUnidadeQuandoEncontrada() {
        when(repository.findAll()).thenReturn(List.of(unidadeMedida));

        UnidadeMedidaDTO result = service.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.codigo());
        verify(repository, times(1)).findAll();
    }


    @Test
    void cadastrarUnidade_DeveSalvarNovaUnidade() {
        when(repository.countByName("Metro")).thenReturn(0L);
        when(repository.save(any(UnidadeMedida.class))).thenReturn(unidadeMedida);

        UnidadeMedidaDTO result = service.cadastrarUnidade(unidadeMedidaDTO);

        assertNotNull(result);
        assertEquals("Metro", result.nome());
        verify(repository, times(1)).countByName("Metro");
        verify(repository, times(1)).save(any(UnidadeMedida.class));
    }

    @Test
    void cadastrarUnidade_DeveLancarExcecaoQuandoNomeDuplicado() {
        when(repository.countByName("Metro")).thenReturn(1L);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class, () -> service.cadastrarUnidade(unidadeMedidaDTO));

        assertEquals("Unidade de medida já cadastrada", exception.getMessage());
        verify(repository, times(1)).countByName("Metro");
        verify(repository, never()).save(any(UnidadeMedida.class));
    }

    @Test
    void atualizarUnidade_DeveAtualizarUnidadeExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(unidadeMedida));
        when(repository.save(any(UnidadeMedida.class))).thenReturn(unidadeMedida);

        UnidadeMedidaDTO novosDados = new UnidadeMedidaDTO(1L, "Metro Atualizado", "m");
        UnidadeMedidaDTO result = service.atualizarUnidade(1L, novosDados);

        assertNotNull(result);
        assertEquals("Metro Atualizado", result.nome());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(UnidadeMedida.class));
    }

    @Test
    void atualizarUnidade_DeveLancarExcecaoQuandoNaoEncontrada() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class, () -> service.atualizarUnidade(1L, unidadeMedidaDTO));

        assertEquals("Unidade com ID 1 não encontrada", exception.getMessage());
        verify(repository, times(1)).findById(1L);
        verify(repository, never()).save(any(UnidadeMedida.class));
    }

    @Test
    void deletarUnidade_DeveDeletarUnidade() {
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deletarUnidade(1L));

        verify(repository, times(1)).deleteById(1L);
    }
}
