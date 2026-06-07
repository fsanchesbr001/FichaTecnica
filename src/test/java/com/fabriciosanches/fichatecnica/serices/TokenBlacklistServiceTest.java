package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.security.TokenBlacklistService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBlacklistServiceTest {

    @Test
    void revogarEConsultar_DeveMarcarTokenComoRevogado() {
        TokenBlacklistService service = new TokenBlacklistService();

        service.revogar("jwt-1", Instant.now().plusSeconds(600));

        assertTrue(service.estaRevogado("jwt-1"));
    }

    @Test
    void limparTokensExpirados_DeveRemoverSomenteExpirados() {
        TokenBlacklistService service = new TokenBlacklistService();

        service.revogar("jwt-expirado", Instant.now().minusSeconds(10));
        service.revogar("jwt-valido", Instant.now().plusSeconds(600));

        service.limparTokensExpirados();

        assertFalse(service.estaRevogado("jwt-expirado"));
        assertTrue(service.estaRevogado("jwt-valido"));
    }
}

