package com.fabriciosanches.fichatecnica.core.ports.in;

import java.io.IOException;

public interface TransferenciaArquivoPort {
    String transferir(byte[] conteudo, String destinationPath, String fileName) throws IOException;
}


