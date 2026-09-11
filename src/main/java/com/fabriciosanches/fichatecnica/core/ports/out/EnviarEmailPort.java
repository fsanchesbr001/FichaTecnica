package com.fabriciosanches.fichatecnica.core.ports.out;

public interface EnviarEmailPort {
    void enviar(String destinatario, String assunto, String corpoTexto);
}

