package com.fabriciosanches.fichatecnica.core.ports.out;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;

import java.time.OffsetDateTime;

public interface GeradorTokenPort {
    String gerarToken(Usuario usuario);

    long getExpirationMinutes();

    OffsetDateTime getTokenExpiresAt();
}

