package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarUnidadeMedidaUseCaseTest {

    @Mock
    private UnidadeMedidaRepositoryPort repositoryPort;

    @InjectMocks
    private AtualizarUnidadeMedidaUseCase useCase;

    @Test
    void atualizar_DeveSalvarQuandoDadosForemValidos() {
        when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(new UnidadeMedida(1L, "Metro", "M")));
        when(repositoryPort.buscarPorSigla("KG")).thenReturn(Optional.empty());
        when(repositoryPort.salvar(any(UnidadeMedida.class))).thenReturn(new UnidadeMedida(1L, "Quilograma", "KG"));

        UnidadeMedida atualizada = useCase.atualizar(1L, " Quilograma ", "kg");

        assertEquals("Quilograma", atualizada.getNome());
        assertEquals("KG", atualizada.getSigla());
        verify(repositoryPort).salvar(any(UnidadeMedida.class));
    }

    @Test
    void atualizar_DeveLancarExcecaoQuandoNaoEncontrarPorId() {
        when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.empty());

        java.util.NoSuchElementException exception =
                assertThrows(java.util.NoSuchElementException.class, () -> useCase.atualizar(1L, "Quilo", "kg"));

        assertEquals("Unidade de medida não encontrada", exception.getMessage());
        verify(repositoryPort, never()).salvar(any(UnidadeMedida.class));
    }

    @Test
    void atualizar_DeveLancarExcecaoQuandoSiglaJaExistirEmOutroRegistro() {
        when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(new UnidadeMedida(1L, "Metro", "M")));
        when(repositoryPort.buscarPorSigla("CM")).thenReturn(Optional.of(new UnidadeMedida(2L, "Centímetro", "CM")));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> useCase.atualizar(1L, "Metro", "cm"));

        assertEquals("Já existe uma unidade de medida com a sigla 'CM'.", exception.getMessage());
        verify(repositoryPort, never()).salvar(any(UnidadeMedida.class));
    }
}
