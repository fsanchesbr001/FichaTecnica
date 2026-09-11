package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.constants.Constants;
import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.in.ControleAcessoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarTokenPort;
import com.fabriciosanches.fichatecnica.enums.UserRole;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.security.DadosTokenJWT;
import com.fabriciosanches.fichatecnica.security.TokenBlacklistService;
import com.fabriciosanches.fichatecnica.security.TokenService;
import com.fabriciosanches.fichatecnica.security.UsuarioSecurityDetails;
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
    private GerarTokenPort gerarTokenPort;
    private ControleAcessoPort controleAcessoPort;
    private TokenBlacklistService tokenBlacklistService;
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        manager = Mockito.mock(AuthenticationManager.class);
        gerarTokenPort = Mockito.mock(GerarTokenPort.class);
        controleAcessoPort = Mockito.mock(ControleAcessoPort.class);
        tokenBlacklistService = Mockito.mock(TokenBlacklistService.class);
        tokenService = Mockito.mock(TokenService.class);

        AutenticacaoController controller = new AutenticacaoController(
                manager,
                gerarTokenPort,
                controleAcessoPort,
                tokenBlacklistService,
                tokenService
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
        UsuarioSecurityDetails details = new UsuarioSecurityDetails(usuario);
        Authentication authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());

        when(manager.authenticate(any())).thenReturn(authentication);
        when(gerarTokenPort.gerarToken(any(Usuario.class))).thenReturn(new DadosTokenJWT(
                "jwt-ok",
                120L,
                OffsetDateTime.parse("2026-05-28T01:00:00-03:00"),
                "admin@email.com",
                "Admin",
                "ROLE_ADMIN"
        ));

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

        verify(controleAcessoPort).errouSenha(eq("admin@email.com"));
    }

    @Test
    void efetuarLogin_DeveRetornarBadRequestQuandoRegraNegocioFalhar() throws Exception {
        doThrow(new FichaTecnicaException("bloqueado")).when(controleAcessoPort).validarAcesso("admin@email.com");

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

