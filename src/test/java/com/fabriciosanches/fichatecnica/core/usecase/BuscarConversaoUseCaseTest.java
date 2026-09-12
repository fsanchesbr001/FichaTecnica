package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarConversaoUseCaseTest {

    @Mock
    private ConversaoRepositoryPort repositoryPort;

    @InjectMocks
    private BuscarConversaoUseCase useCase;

    @Test
    void buscarTodos_DeveRetornarListaOrdenadaPorUnidadeDe() {
        when(repositoryPort.buscarTodos()).thenReturn(List.of(
                new Conversao(2L, 5L, 1L, "MULTIPLICA", BigDecimal.ONE),
                new Conversao(1L, 2L, 1L, "MULTIPLICA", BigDecimal.ONE)
        ));

        List<Conversao> resultado = useCase.buscarTodos();

        assertEquals(List.of(2L, 5L), resultado.stream().map(Conversao::getUnidadeDe).toList());
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoNaoEncontrar() {
        when(repositoryPort.buscarPorId(10L)).thenReturn(Optional.empty());

        java.util.NoSuchElementException exception =
                assertThrows(java.util.NoSuchElementException.class, () -> useCase.buscarPorId(10L));

        assertEquals("ConversÃ£o com ID 10 nÃ£o encontrada", exception.getMessage());
    }
}

