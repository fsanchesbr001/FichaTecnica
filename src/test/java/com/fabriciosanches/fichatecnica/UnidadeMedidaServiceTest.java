package com.fabriciosanches.fichatecnica;

import com.fabriciosanches.fichatecnica.domain.medidas.DadosUnidadeMedida;
import com.fabriciosanches.fichatecnica.domain.medidas.UnidadeMedida;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.UnidadeMedidaRepository;
import com.fabriciosanches.fichatecnica.services.UnidadeMedidaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnidadeMedidaServiceTest {

    @Mock
    private UnidadeMedidaRepository repository;

    @InjectMocks
    private UnidadeMedidaService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listar_DeveRetornarListaOrdenada() {
        UnidadeMedida unidade1 = new UnidadeMedida(1L, "Unidade A", "UA");
        UnidadeMedida unidade2 = new UnidadeMedida(2L, "Unidade B", "UB");
        when(repository.findAll()).thenReturn(List.of(unidade2, unidade1));

        List<DadosUnidadeMedida> result = service.listar();

        assertEquals(2, result.size());
        assertEquals("Unidade A", result.get(0).nome());
        assertEquals("Unidade B", result.get(1).nome());
    }

    @Test
    void buscarPorId_DeveRetornarUnidadeQuandoEncontrada() {
        UnidadeMedida unidade = new UnidadeMedida(1L, "Unidade A", "UA");
        when(repository.findAll()).thenReturn(List.of(unidade));

        DadosUnidadeMedida result = service.buscarPorId(1L);

        assertNotNull(result);
        assertEquals("Unidade A", result.nome());
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoNaoEncontrada() {
        when(repository.findAll()).thenReturn(List.of());

        assertThrows(FichaTecnicaException.class, () -> service.buscarPorId(1L));
    }

    @Test
    void cadastrarUnidade_DeveSalvarUnidade() {
        DadosUnidadeMedida dados = new DadosUnidadeMedida(1L, "Unidade A", "UA");
        UnidadeMedida unidade = new UnidadeMedida(dados);
        when(repository.save(any(UnidadeMedida.class))).thenReturn(unidade);
        when(repository.countByName(anyString())).thenReturn(0L);

        DadosUnidadeMedida result = service.cadastrarUnidade(dados);

        assertNotNull(result);
        assertEquals("Unidade A", result.nome());
    }

    @Test
    void atualizarUnidade_DeveAtualizarUnidadeExistente() {
        DadosUnidadeMedida novosDados = new DadosUnidadeMedida(1L, "Unidade Atualizada", "UA");
        UnidadeMedida unidadeExistente = new UnidadeMedida(1L, "Unidade A", "UA");
        when(repository.findById(1L)).thenReturn(Optional.of(unidadeExistente));
        when(repository.save(any(UnidadeMedida.class))).thenReturn(unidadeExistente);

        DadosUnidadeMedida result = service.atualizarUnidade(1L, novosDados);

        assertNotNull(result);
        assertEquals("Unidade Atualizada", result.nome());
    }

    @Test
    void deletarUnidade_DeveDeletarUnidade() {
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deletarUnidade(1L));
        verify(repository, times(1)).deleteById(1L);
    }
}
