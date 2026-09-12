package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.out.GeradorRelatorioUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerarRelatorioListaUnidadeMedidaUseCaseTest {

    @Mock
    private UnidadeMedidaRepositoryPort repositoryPort;

    @Mock
    private GeradorRelatorioUnidadeMedidaPort reportPort;

    @InjectMocks
    private GerarRelatorioListaUnidadeMedidaUseCase useCase;

    @Test
    void executar_DeveGerarRelatorioComListaOrdenadaPorNome() {
        when(repositoryPort.buscarTodos()).thenReturn(List.of(
                new UnidadeMedida(2L, "Metro", "M"),
                new UnidadeMedida(1L, "CentÃ­metro", "CM")
        ));
        when(reportPort.gerarRelatorioLista(anyList())).thenReturn(new byte[]{1, 2, 3});

        byte[] resultado = useCase.executar();

        assertArrayEquals(new byte[]{1, 2, 3}, resultado);
        ArgumentCaptor<List<UnidadeMedida>> captor = ArgumentCaptor.forClass(List.class);
        verify(reportPort).gerarRelatorioLista(captor.capture());
        assertEquals(List.of("CentÃ­metro", "Metro"), captor.getValue().stream().map(UnidadeMedida::getNome).toList());
    }

    @Test
    void executar_DeveLancarExcecaoQuandoNaoHouverUnidades() {
        when(repositoryPort.buscarTodos()).thenReturn(List.of());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, useCase::executar);

        assertEquals("Nenhuma unidade de medida encontrada para gerar relatÃ³rio", exception.getMessage());
    }
}

