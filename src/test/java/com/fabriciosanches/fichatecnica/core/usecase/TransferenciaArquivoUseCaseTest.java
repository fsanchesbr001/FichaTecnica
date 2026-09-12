package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.ports.out.ArmazenamentoArquivoPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class TransferenciaArquivoUseCaseTest {

    @Test
    void transferir_DeveDelegarParaPortaDeArmazenamento() throws IOException {
        ArmazenamentoArquivoPort armazenamento = Mockito.mock(ArmazenamentoArquivoPort.class);
        TransferenciaArquivoUseCase useCase = new TransferenciaArquivoUseCase(armazenamento);
        byte[] conteudo = "abc".getBytes();

        when(armazenamento.salvar(eq(conteudo), eq("/tmp"), eq("foto.png"))).thenReturn("/tmp/id.png");

        String path = useCase.transferir(conteudo, "/tmp", "foto.png");

        assertEquals("/tmp/id.png", path);
    }

    @Test
    void transferir_DeveFalharQuandoArquivoVazio() {
        ArmazenamentoArquivoPort armazenamento = Mockito.mock(ArmazenamentoArquivoPort.class);
        TransferenciaArquivoUseCase useCase = new TransferenciaArquivoUseCase(armazenamento);

        assertThrows(IllegalArgumentException.class, () -> useCase.transferir(new byte[0], "/tmp", "a.txt"));
    }
}

