package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.constants.Constants;
import com.fabriciosanches.fichatecnica.controllers.AutenticacaoController;
import com.fabriciosanches.fichatecnica.domains.Usuario;
import com.fabriciosanches.fichatecnica.enums.UserRole;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.security.TokenBlacklistService;
import com.fabriciosanches.fichatecnica.security.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutenticacaoControllerTest {

    private MockMvc mockMvc;
    private AuthenticationManager manager;
    private TokenService tokenService;
    private SegurancaService segurancaService;
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        manager = Mockito.mock(AuthenticationManager.class);
        tokenService = Mockito.mock(TokenService.class);
        segurancaService = Mockito.mock(SegurancaService.class);
        tokenBlacklistService = Mockito.mock(TokenBlacklistService.class);

        AutenticacaoController controller = new AutenticacaoController(
                manager,
                tokenService,
                segurancaService,
                tokenBlacklistService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void efetuarLogin_DeveRetornarBadRequestQuandoDadosAusentes() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void efetuarLogin_DeveRetornarOkQuandoAutenticacaoForValida() throws Exception {
        Usuario usuario = new Usuario(1L, "admin@email.com", "senha", UserRole.ADMIN, "Admin");
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        when(manager.authenticate(any())).thenReturn(authentication);
        when(tokenService.gerarToken(usuario)).thenReturn("jwt-ok");
        when(tokenService.getExpirationMinutes()).thenReturn(120L);
        when(tokenService.getTokenExpiresAt()).thenReturn(OffsetDateTime.parse("2026-05-28T01:00:00-03:00"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "admin@email.com",
                                  "senha": "123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("jwt-ok"))
                .andExpect(jsonPath("$.usuarioLogin").value("admin@email.com"));
    }

    @Test
    void efetuarLogin_DeveRetornarBadRequestQuandoCredenciaisInvalidas() throws Exception {
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("invalid"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "admin@email.com",
                                  "senha": "errada"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.jwt").value(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS));

        verify(segurancaService).errouSenha(eq("admin@email.com"));
    }

    @Test
    void efetuarLogin_DeveRetornarBadRequestQuandoRegraNegocioFalhar() throws Exception {
        doThrow(new FichaTecnicaException("bloqueado")).when(segurancaService).validarAcesso("admin@email.com");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "admin@email.com",
                                  "senha": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.jwt").value("bloqueado"));
    }

    @Test
    void efetuarLogout_DeveRetornarBadRequestQuandoNaoHouverSessao() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void efetuarLogout_DeveRetornarBadRequestQuandoTokenSemExpiracao() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "token-sem-exp")
        );
        when(tokenService.getExpiration("token-sem-exp")).thenReturn(null);

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void efetuarLogout_DeveRetornarOkQuandoSessaoValida() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "jwt-valido")
        );
        Instant exp = Instant.parse("2026-05-28T04:00:00Z");
        when(tokenService.getExpiration("jwt-valido")).thenReturn(exp);

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(tokenBlacklistService).revogar("jwt-valido", exp);
    }
}

