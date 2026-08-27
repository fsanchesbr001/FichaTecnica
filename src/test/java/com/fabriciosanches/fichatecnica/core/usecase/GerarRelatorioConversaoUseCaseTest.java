package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;
import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;
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
class GerarRelatorioConversaoUseCaseTest {

    @Mock
    private ConversaoRepositoryPort repositoryPort;

    @InjectMocks
    private GerarRelatorioConversaoUseCase useCase;

    @Test
    void buscarTodosComNomes_DeveDelegarParaRepository() {
        when(repositoryPort.buscarTodosComNomes()).thenReturn(List.of(
                new ConversaoRelatorioDTO(1L, "Quilo", "Grama", "MULTIPLICA", new BigDecimal("1000.00"))
        ));

        List<ConversaoRelatorioDTO> resultado = useCase.buscarTodosComNomes();

        assertEquals(1, resultado.size());
        assertEquals("Quilo", resultado.get(0).unidadeDe());
    }

    @Test
    void buscarPorIdComNomes_DeveLancarExcecaoQuandoNaoEncontrar() {
        when(repositoryPort.buscarPorIdComNomes(1L)).thenReturn(Optional.empty());

        java.util.NoSuchElementException exception =
                assertThrows(java.util.NoSuchElementException.class, () -> useCase.buscarPorIdComNomes(1L));

        assertEquals("Conversão com ID 1 não encontrada", exception.getMessage());
    }
}
