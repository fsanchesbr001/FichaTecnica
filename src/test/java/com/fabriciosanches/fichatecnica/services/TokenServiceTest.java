package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.Usuario;
import com.fabriciosanches.fichatecnica.enums.UserRole;
import com.fabriciosanches.fichatecnica.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService service;

    @BeforeEach
    void setUp() {
        service = new TokenService();
        ReflectionTestUtils.setField(service, "secret", "segredo-testes");
        ReflectionTestUtils.setField(service, "expirationMinutes", 30L);
        ReflectionTestUtils.setField(service, "tokenTimeZone", "America/Sao_Paulo");
    }

    @Test
    void gerarToken_DevePermitirExtrairSubjectRoleEExpiracao() {
        Usuario usuario = new Usuario(1L, "admin@email.com", "senha", UserRole.ADMIN, "Administrador");

        String token = service.gerarToken(usuario);

        assertNotNull(token);
        assertEquals("admin@email.com", service.getSubject(token));
        assertEquals("ROLE_ADMIN", service.getRole(token));
        assertTrue(service.validarTokenExpirado(token));

        Instant expiracao = service.getExpiration(token);
        assertNotNull(expiracao);
        assertTrue(expiracao.isAfter(Instant.now()));
    }

    @Test
    void validarTokenExpirado_DeveRetornarFalseParaTokenExpirado() {
        ReflectionTestUtils.setField(service, "expirationMinutes", -1L);
        Usuario usuario = new Usuario(1L, "admin@email.com", "senha", UserRole.ADMIN, "Administrador");

        String tokenExpirado = service.gerarToken(usuario);

        assertFalse(service.validarTokenExpirado(tokenExpirado));
    }

    @Test
    void metodosDeLeitura_DeveFalharQuandoTokenForInvalido() {
        String tokenInvalido = "token.invalido.aqui";

        RuntimeException exSubject = assertThrows(RuntimeException.class, () -> service.getSubject(tokenInvalido));
        RuntimeException exRole = assertThrows(RuntimeException.class, () -> service.getRole(tokenInvalido));

        assertTrue(exSubject.getMessage().contains("Verificação de Token falhou"));
        assertTrue(exRole.getMessage().contains("Verificação de Token falhou"));
        assertNull(service.getExpiration(tokenInvalido));
        assertFalse(service.validarTokenExpirado(tokenInvalido));
    }
}

