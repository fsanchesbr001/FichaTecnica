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
import org.springframework.dao.DataIntegrityViolationException;

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
    private UnidadeMedida unidadeAlternativa;


    @BeforeEach
    void setUp() {
        unidadeMedida = new UnidadeMedida(1L, "Metro", "m");
        unidadeMedidaDTO = new UnidadeMedidaDTO(1L, "Metro", "m");
        unidadeAlternativa = new UnidadeMedida(2L, "Centímetro", "cm");
    }

    @Test
    void listar_DeveRetornarListaOrdenada() {
        when(repository.findAll()).thenReturn(List.of(unidadeMedida, unidadeAlternativa));

        List<UnidadeMedidaDTO> result = service.listar();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Centímetro", result.get(0).nome());
        assertEquals("Metro", result.get(1).nome());
        verify(repository, times(1)).findAll();
    }


    @Test
    void buscarPorId_DeveRetornarUnidadeQuandoEncontrada() {
        when(repository.findAll()).thenReturn(List.of(unidadeAlternativa, unidadeMedida));

        UnidadeMedidaDTO result = service.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.codigo());
        verify(repository, times(1)).findAll();
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoNaoEncontrada() {
        when(repository.findAll()).thenReturn(List.of(unidadeAlternativa));

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.buscarPorId(99L));

        assertEquals("Unidade de medida não encontrada", exception.getMessage());
    }

    @Test
    void findByNameEFindBySigla_DevemDelegarParaRepository() {
        when(repository.countByName("Metro")).thenReturn(1L);
        when(repository.countBySigla("m")).thenReturn(2L);

        assertEquals(1L, service.findByName("Metro"));
        assertEquals(2L, service.findBySigla("m"));
    }


    @Test
    void cadastrarUnidade_DeveSalvarNovaUnidade() {
        when(repository.countBySigla("m")).thenReturn(0L);
        when(repository.save(any(UnidadeMedida.class))).thenReturn(unidadeMedida);

        UnidadeMedidaDTO result = service.cadastrarUnidade(unidadeMedidaDTO);

        assertNotNull(result);
        assertEquals("Metro", result.nome());
        verify(repository, times(1)).countBySigla("m");
        verify(repository, times(1)).save(any(UnidadeMedida.class));
    }

    @Test
    void cadastrarUnidade_DeveLancarExcecaoQuandoSiglaDuplicada() {
        when(repository.countBySigla("m")).thenReturn(1L);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class, () -> service.cadastrarUnidade(unidadeMedidaDTO));

        assertEquals("Já existe uma unidade de medida com a sigla 'm'.", exception.getMessage());
        verify(repository, times(1)).countBySigla("m");
        verify(repository, never()).save(any(UnidadeMedida.class));
    }

    @Test
    void cadastrarUnidade_DeveLancarExcecaoQuandoUnidadeForNula() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> service.cadastrarUnidade(null));

        assertEquals("Unidade de medida não pode ser nula", exception.getMessage());
    }

    @Test
    void atualizarUnidade_DeveAtualizarUnidadeExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(unidadeMedida));
        when(repository.save(any(UnidadeMedida.class))).thenReturn(unidadeMedida);
        when(repository.countBySiglaAndCodigoNot("km", 1L)).thenReturn(0L);

        UnidadeMedidaDTO novosDados = new UnidadeMedidaDTO(1L, "Quilômetro", "km");
        UnidadeMedidaDTO result = service.atualizarUnidade(1L, novosDados);

        assertNotNull(result);
        assertEquals("Quilômetro", result.nome());
        assertEquals("km", result.sigla());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(UnidadeMedida.class));
    }

    @Test
    void atualizarUnidade_DeveLancarExcecaoQuandoSiglaJaExistirEmOutroRegistro() {
        when(repository.findById(1L)).thenReturn(Optional.of(unidadeMedida));
        when(repository.countBySiglaAndCodigoNot("cm", 1L)).thenReturn(1L);

        UnidadeMedidaDTO novosDados = new UnidadeMedidaDTO(1L, "Metro", "cm");

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.atualizarUnidade(1L, novosDados));

        assertEquals("Já existe uma unidade de medida com a sigla 'cm'.", exception.getMessage());
        verify(repository, never()).save(any(UnidadeMedida.class));
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
        doNothing().when(repository).flush();

        assertDoesNotThrow(() -> service.deletarUnidade(1L));

        verify(repository, times(1)).deleteById(1L);
        verify(repository, times(1)).flush();
    }

    @Test
    void deletarUnidade_DeveLancarExcecaoQuandoHouverViolacaoDeIntegridade() {
        doNothing().when(repository).deleteById(1L);
        doThrow(new DataIntegrityViolationException("fk")).when(repository).flush();

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.deletarUnidade(1L));

        assertEquals("FKC-Registro não pode ser deletado. Existem Conversões vinculadas.", exception.getMessage());
    }
}
