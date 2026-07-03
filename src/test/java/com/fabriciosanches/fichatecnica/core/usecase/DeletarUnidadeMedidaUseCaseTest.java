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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletarUnidadeMedidaUseCaseTest {

    @Mock
    private UnidadeMedidaRepositoryPort repositoryPort;

    @InjectMocks
    private DeletarUnidadeMedidaUseCase useCase;

    @Test
    void deletar_DeveRemoverQuandoExistir() {
        when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(new UnidadeMedida(1L, "Metro", "M")));

        useCase.deletar(1L);

        verify(repositoryPort).deletar(1L);
    }

    @Test
    void deletar_DeveLancarExcecaoQuandoNaoExistir() {
        when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.empty());

        java.util.NoSuchElementException exception =
                assertThrows(java.util.NoSuchElementException.class, () -> useCase.deletar(1L));

        assertEquals("Unidade de medida não encontrada", exception.getMessage());
    }
}
