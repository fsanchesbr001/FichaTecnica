package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarUnidadeMedidaUseCaseTest {

    @Mock
    private UnidadeMedidaRepositoryPort repositoryPort;

    @InjectMocks
    private CriarUnidadeMedidaUseCase useCase;

    @Test
    void criar_DevePersistirQuandoSiglaNaoExiste() {
        when(repositoryPort.buscarPorSigla("M")).thenReturn(Optional.empty());
        when(repositoryPort.salvar(org.mockito.ArgumentMatchers.any(UnidadeMedida.class)))
                .thenReturn(new UnidadeMedida(1L, "Metro", "M"));

        UnidadeMedida unidadeCriada = useCase.criar("  Metro  ", " m ");

        ArgumentCaptor<UnidadeMedida> captor = ArgumentCaptor.forClass(UnidadeMedida.class);
        verify(repositoryPort).salvar(captor.capture());

        UnidadeMedida unidadePersistida = captor.getValue();
        assertEquals("Metro", unidadePersistida.getNome());
        assertEquals("M", unidadePersistida.getSigla());
        assertEquals(1L, unidadeCriada.getCodigo());
        assertEquals("M", unidadeCriada.getSigla());
    }

    @Test
    void criar_DeveLancarExcecaoQuandoSiglaJaExiste() {
        when(repositoryPort.buscarPorSigla("M")).thenReturn(Optional.of(new UnidadeMedida(1L, "Metro", "M")));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> useCase.criar("Mililitro", "m"));

        assertEquals("Já existe uma unidade de medida com a sigla 'M'.", exception.getMessage());
        verify(repositoryPort, never()).salvar(org.mockito.ArgumentMatchers.any(UnidadeMedida.class));
    }

    @Test
    void criar_DeveLancarExcecaoQuandoNomeForInvalido() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> useCase.criar("  ", "m"));

        assertEquals("Nome não pode ser vazio", exception.getMessage());
        verify(repositoryPort, never()).buscarPorSigla(org.mockito.ArgumentMatchers.anyString());
        verify(repositoryPort, never()).salvar(org.mockito.ArgumentMatchers.any(UnidadeMedida.class));
    }

    @Test
    void criar_DeveLancarExcecaoQuandoSiglaForInvalida() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> useCase.criar("Metro", null));

        assertEquals("Sigla não pode ser vazia", exception.getMessage());
        verify(repositoryPort, never()).buscarPorSigla(org.mockito.ArgumentMatchers.anyString());
        verify(repositoryPort, never()).salvar(org.mockito.ArgumentMatchers.any(UnidadeMedida.class));
    }
}
