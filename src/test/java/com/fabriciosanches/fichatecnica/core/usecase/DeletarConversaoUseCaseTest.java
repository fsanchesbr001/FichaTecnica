package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeletarConversaoUseCaseTest {

    @Mock
    private ConversaoRepositoryPort repositoryPort;

    @InjectMocks
    private DeletarConversaoUseCase useCase;

    @Test
    void deletar_DeveRemoverQuandoIdForValido() {
        useCase.deletar(1L);
        verify(repositoryPort).deletar(1L);
    }

    @Test
    void deletar_DeveLancarExcecaoQuandoIdForNulo() {
        assertThrows(IllegalArgumentException.class, () -> useCase.deletar(null));
    }
}
