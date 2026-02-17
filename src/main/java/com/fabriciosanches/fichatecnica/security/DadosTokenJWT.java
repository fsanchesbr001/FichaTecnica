package com.fabriciosanches.fichatecnica.security;

import java.time.OffsetDateTime;

public record DadosTokenJWT(String jwt, Long expirationMinutes, OffsetDateTime expiresAt) {

    // Construtor compatível com código antigo que passa apenas jwt
    public DadosTokenJWT(String jwt) {
        this(jwt, null, null);
    }
}
