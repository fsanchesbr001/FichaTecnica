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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarConversaoUseCaseTest {

    @Mock
    private ConversaoRepositoryPort repositoryPort;

    @InjectMocks
    private CriarConversaoUseCase useCase;

    @Test
    void criar_DevePersistirQuandoConversaoForValida() {
        Conversao conversao = new Conversao(2L, 3L, "MULTIPLICA", new BigDecimal("2.00"));
        when(repositoryPort.contarPorUnidadeDeEUnidadePara(2L, 3L)).thenReturn(0L);
        when(repositoryPort.salvar(any(Conversao.class)))
                .thenReturn(new Conversao(1L, 2L, 3L, "MULTIPLICA", new BigDecimal("2.00")));

        Conversao criada = useCase.criar(conversao);

        ArgumentCaptor<Conversao> captor = ArgumentCaptor.forClass(Conversao.class);
        verify(repositoryPort).salvar(captor.capture());
        assertEquals(2L, captor.getValue().getUnidadeDe());
        assertEquals(1L, criada.getCodigo());
    }

    @Test
    void criar_DeveLancarExcecaoQuandoDuplicada() {
        Conversao conversao = new Conversao(2L, 3L, "MULTIPLICA", new BigDecimal("2.00"));
        when(repositoryPort.contarPorUnidadeDeEUnidadePara(2L, 3L)).thenReturn(1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> useCase.criar(conversao));

        assertEquals("Conversão já cadastrada", exception.getMessage());
        verify(repositoryPort, never()).salvar(any(Conversao.class));
    }
}
