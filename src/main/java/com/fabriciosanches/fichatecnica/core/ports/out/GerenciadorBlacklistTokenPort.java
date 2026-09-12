package com.fabriciosanches.fichatecnica.core.ports.out;

import java.time.Instant;

public interface GerenciadorBlacklistTokenPort {
    void revogar(String token, Instant expiresAt);

    boolean estaRevogado(String token);

    void limparTokensExpirados();
}


