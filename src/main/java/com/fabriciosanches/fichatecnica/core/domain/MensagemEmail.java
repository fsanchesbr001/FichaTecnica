package com.fabriciosanches.fichatecnica.core.domain;

public record MensagemEmail(String destinatario, String assunto, String conteudo, boolean html) {
}
