package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarUnidadeMedidaUseCaseTest {

    @Mock
    private UnidadeMedidaRepositoryPort repositoryPort;

    @InjectMocks
    private BuscarUnidadeMedidaUseCase useCase;

    @Test
    void buscarTodos_DeveRetornarListaOrdenadaPorNome() {
        when(repositoryPort.buscarTodos()).thenReturn(List.of(
                new UnidadeMedida(2L, "Metro", "M"),
                new UnidadeMedida(1L, "Centímetro", "CM")
        ));

        List<UnidadeMedida> resultado = useCase.buscarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Centímetro", resultado.get(0).getNome());
        assertEquals("Metro", resultado.get(1).getNome());
    }

    @Test
    void buscarPorId_DeveRetornarQuandoEncontrar() {
        when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(new UnidadeMedida(1L, "Metro", "M")));

        UnidadeMedida resultado = useCase.buscarPorId(1L);

        assertEquals(1L, resultado.getCodigo());
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoNaoEncontrar() {
        when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.empty());

        java.util.NoSuchElementException exception =
                assertThrows(java.util.NoSuchElementException.class, () -> useCase.buscarPorId(1L));

        assertEquals("Unidade de medida não encontrada", exception.getMessage());
    }
}
