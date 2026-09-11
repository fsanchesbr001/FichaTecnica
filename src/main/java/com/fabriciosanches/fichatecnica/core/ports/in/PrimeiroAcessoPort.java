package com.fabriciosanches.fichatecnica.core.ports.in;

import jakarta.mail.MessagingException;

public interface PrimeiroAcessoPort {
    void primeiroAcesso(String email) throws MessagingException;
}

