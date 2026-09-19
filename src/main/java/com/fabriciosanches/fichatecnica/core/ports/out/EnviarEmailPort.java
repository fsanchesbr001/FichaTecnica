package com.fabriciosanches.fichatecnica.core.ports.out;

import java.util.Map;

public interface EnviarEmailPort {
    void enviar(String destinatario, String assunto, String corpoTexto);
    void enviarComTemplate(String destinatario, String assunto, String nomeTemplate, Map<String, Object> variaveis);
}


