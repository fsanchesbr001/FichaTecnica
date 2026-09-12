package com.fabriciosanches.fichatecnica.infrastructure.config.security;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.in.AutenticarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.out.GerenciadorBlacklistTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ValidadorTokenPort;
import com.fabriciosanches.fichatecnica.enums.UserRole;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private ValidadorTokenPort validadorTokenPort;
    @Mock
    private GerenciadorBlacklistTokenPort gerenciadorBlacklistTokenPort;
    @Mock
    private AutenticarUsuarioPort autenticarUsuarioPort;
    @Mock
    private FilterChain filterChain;

    private SecurityFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SecurityFilter(validadorTokenPort, autenticarUsuarioPort, gerenciadorBlacklistTokenPort);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_DevePermitirPreflightOptionsSemToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/qualquer");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_DeveRetornar401QuandoTokenExpirado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/produtos");
        request.addHeader("Authorization", "Bearer token-expirado");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(validadorTokenPort.validarTokenExpirado("token-expirado")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token expirado"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_DeveAutenticarQuandoTokenValido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/produtos");
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Usuario usuario = new Usuario(1L, "admin@email.com", "senha", UserRole.ADMIN, "Admin");

        when(validadorTokenPort.validarTokenExpirado("token-valido")).thenReturn(true);
        when(gerenciadorBlacklistTokenPort.estaRevogado("token-valido")).thenReturn(false);
        when(validadorTokenPort.getSubject("token-valido")).thenReturn("admin@email.com");
        when(validadorTokenPort.getRole("token-valido")).thenReturn("ROLE_ADMIN");
        when(autenticarUsuarioPort.buscarPorLogin("admin@email.com")).thenReturn(usuario);

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(usuario, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }
}

