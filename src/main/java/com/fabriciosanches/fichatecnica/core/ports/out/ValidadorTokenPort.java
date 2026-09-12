package com.fabriciosanches.fichatecnica.core.ports.out;

import java.time.Instant;

public interface ValidadorTokenPort {
    String getSubject(String tokenJWT);

    String getRole(String tokenJWT);

    Instant getExpiration(String tokenJWT);

    boolean validarTokenExpirado(String tokenJWT);
}


