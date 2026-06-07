package com.fabriciosanches.fichatecnica.security;

import com.fabriciosanches.fichatecnica.domains.Usuario;
import com.fabriciosanches.fichatecnica.enums.UserRole;
import com.fabriciosanches.fichatecnica.repository.UsuarioRepository;
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
    private TokenService tokenService;
    @Mock
    private UsuarioRepository repository;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private FilterChain filterChain;

    private SecurityFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SecurityFilter(tokenService, repository, tokenBlacklistService);
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

        when(tokenService.validarTokenExpirado("token-expirado")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token expirado"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_DeveRetornar401QuandoTokenRevogado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/produtos");
        request.addHeader("Authorization", "Bearer token-revogado");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenService.validarTokenExpirado("token-revogado")).thenReturn(true);
        when(tokenBlacklistService.estaRevogado("token-revogado")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token revogado"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_DeveAutenticarQuandoTokenValido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/produtos");
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Usuario usuario = new Usuario(1L, "admin@email.com", "senha", UserRole.ADMIN, "Admin");

        when(tokenService.validarTokenExpirado("token-valido")).thenReturn(true);
        when(tokenBlacklistService.estaRevogado("token-valido")).thenReturn(false);
        when(tokenService.getSubject("token-valido")).thenReturn("admin@email.com");
        when(tokenService.getRole("token-valido")).thenReturn("ROLE_ADMIN");
        when(repository.findByLogin("admin@email.com")).thenReturn(usuario);

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(usuario, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }
}

