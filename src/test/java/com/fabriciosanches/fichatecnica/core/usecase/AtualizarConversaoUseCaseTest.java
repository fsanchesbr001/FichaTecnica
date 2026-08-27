package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarConversaoUseCaseTest {

    @Mock
    private ConversaoRepositoryPort repositoryPort;

    @InjectMocks
    private AtualizarConversaoUseCase useCase;

    @Test
    void atualizar_DeveSalvarComIdInformado() {
        when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(new Conversao(1L, 2L, 3L, "MULTIPLICA", BigDecimal.ONE)));
        when(repositoryPort.salvar(any(Conversao.class)))
                .thenReturn(new Conversao(1L, 4L, 5L, "DIVIDE", new BigDecimal("2.00")));

        Conversao entrada = new Conversao(99L, 4L, 5L, "DIVIDE", new BigDecimal("2.00"));
        Conversao atualizada = useCase.atualizar(1L, entrada);

        ArgumentCaptor<Conversao> captor = ArgumentCaptor.forClass(Conversao.class);
        verify(repositoryPort).salvar(captor.capture());
        assertEquals(1L, captor.getValue().getCodigo());
        assertEquals("DIVIDE", atualizada.getOperacao());
    }

    @Test
    void atualizar_DeveLancarExcecaoQuandoNaoEncontrarPorId() {
        when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.empty());

        java.util.NoSuchElementException exception = assertThrows(
                java.util.NoSuchElementException.class,
                () -> useCase.atualizar(1L, new Conversao(2L, 3L, "MULTIPLICA", BigDecimal.ONE))
        );

        assertEquals("Conversão com ID 1 não encontrada", exception.getMessage());
        verify(repositoryPort, never()).salvar(any(Conversao.class));
    }
}
