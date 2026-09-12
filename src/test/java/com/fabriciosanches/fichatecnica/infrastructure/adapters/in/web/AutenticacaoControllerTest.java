package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.in.ControleAcessoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.out.GerenciadorBlacklistTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ValidadorTokenPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.AutenticacaoDTO;
import com.fabriciosanches.fichatecnica.core.domain.enums.UserRole;
import com.fabriciosanches.fichatecnica.infrastructure.config.security.DadosTokenJWT;
import com.fabriciosanches.fichatecnica.infrastructure.config.security.UsuarioSecurityDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutenticacaoControllerTest {

    private AuthenticationManager manager;
    private GerarTokenPort gerarTokenPort;
    private ControleAcessoPort controleAcessoPort;
    private GerenciadorBlacklistTokenPort gerenciadorBlacklistTokenPort;
    private ValidadorTokenPort validadorTokenPort;
    private AutenticacaoController controller;

    @BeforeEach
    void setUp() {
        manager = Mockito.mock(AuthenticationManager.class);
        gerarTokenPort = Mockito.mock(GerarTokenPort.class);
        controleAcessoPort = Mockito.mock(ControleAcessoPort.class);
        gerenciadorBlacklistTokenPort = Mockito.mock(GerenciadorBlacklistTokenPort.class);
        validadorTokenPort = Mockito.mock(ValidadorTokenPort.class);

        controller = new AutenticacaoController(
                manager,
                gerarTokenPort,
                controleAcessoPort,
                gerenciadorBlacklistTokenPort,
                validadorTokenPort
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void efetuarLogin_DeveRetornarTokenQuandoCredenciaisValidas() {
        Usuario usuario = new Usuario(1L, "admin@email.com", "senha", UserRole.ADMIN, "Admin");
        UsuarioSecurityDetails details = new UsuarioSecurityDetails(usuario);

        when(manager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(details, "jwt", details.getAuthorities()));
        when(gerarTokenPort.gerarToken(any(Usuario.class))).thenReturn(new DadosTokenJWT("jwt"));

        ResponseEntity<DadosTokenJWT> response = controller.efetuarLogin(new AutenticacaoDTO("admin@email.com", "senha"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("jwt", response.getBody().jwt());
    }

    @Test
    void efetuarLogout_DeveRevogarTokenQuandoTokenValido() {
        String token = "jwt.token";
        Instant expiration = Instant.now().plusSeconds(1200);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("u", token));
        when(validadorTokenPort.getExpiration(token)).thenReturn(expiration);

        ResponseEntity<Map<String, String>> response = controller.efetuarLogout();

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("message").contains("Logout"));
        verify(gerenciadorBlacklistTokenPort).revogar(token, expiration);
    }
}


