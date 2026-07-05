package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.out.GeradorRelatorioUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerarRelatorioDetalheUnidadeMedidaUseCaseTest {

    @Mock
    private UnidadeMedidaRepositoryPort repositoryPort;

    @Mock
    private GeradorRelatorioUnidadeMedidaPort reportPort;

    @InjectMocks
    private GerarRelatorioDetalheUnidadeMedidaUseCase useCase;

    @Test
    void executar_DeveGerarRelatorioPorSiglaNormalizada() {
        UnidadeMedida unidade = new UnidadeMedida(1L, "Quilograma", "KG");
        when(repositoryPort.buscarPorSigla("KG")).thenReturn(Optional.of(unidade));
        when(reportPort.gerarRelatorioDetalhe(unidade)).thenReturn(new byte[]{9, 8, 7});

        byte[] resultado = useCase.executar(" kg ");

        assertArrayEquals(new byte[]{9, 8, 7}, resultado);
        verify(repositoryPort).buscarPorSigla("KG");
        verify(reportPort).gerarRelatorioDetalhe(unidade);
    }

    @Test
    void executar_DeveLancarExcecaoQuandoSiglaForVazia() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> useCase.executar(" "));

        assertEquals("Sigla da unidade de medida não pode ser vazia", exception.getMessage());
    }

    @Test
    void executar_DeveLancarExcecaoQuandoUnidadeNaoEncontrada() {
        when(repositoryPort.buscarPorSigla("KG")).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> useCase.executar("kg"));

        assertEquals("Unidade de medida não encontrada: KG", exception.getMessage());
    }
}
