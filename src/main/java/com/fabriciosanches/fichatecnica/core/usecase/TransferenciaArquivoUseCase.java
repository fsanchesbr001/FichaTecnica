package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.ports.in.TransferenciaArquivoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ArmazenamentoArquivoPort;

import java.io.IOException;
import java.util.Objects;

public class TransferenciaArquivoUseCase implements TransferenciaArquivoPort {

    private final ArmazenamentoArquivoPort armazenamentoArquivoPort;

    public TransferenciaArquivoUseCase(ArmazenamentoArquivoPort armazenamentoArquivoPort) {
        this.armazenamentoArquivoPort = Objects.requireNonNull(armazenamentoArquivoPort, "ArmazenamentoArquivoPort não pode ser nulo");
    }

    @Override
    public String transferir(byte[] conteudo, String destinationPath, String fileName) throws IOException {
        if (conteudo == null || conteudo.length == 0) {
            throw new IllegalArgumentException("Arquivo não pode ser vazio");
        }
        if (destinationPath == null || destinationPath.isBlank()) {
            throw new IllegalArgumentException("Caminho de destino é obrigatório");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Nome do arquivo é obrigatório");
        }
        return armazenamentoArquivoPort.salvar(conteudo, destinationPath, fileName);
    }
}

