package com.fabriciosanches.fichatecnica.core.ports.out;

import java.io.IOException;

public interface ArmazenamentoArquivoPort {
    String salvar(byte[] conteudo, String destinationPath, String fileName) throws IOException;

    byte[] carregar(String relativePath) throws IOException;

    void deletar(String relativePath) throws IOException;
}

