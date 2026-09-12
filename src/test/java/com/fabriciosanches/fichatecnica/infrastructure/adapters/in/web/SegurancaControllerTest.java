package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RecuperacaoSenhaPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.EnviarEmailSegurancaResponseDTO;
import com.fabriciosanches.fichatecnica.core.domain.enums.UserRole;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.config.FichaTecnicaProperty;
import com.fabriciosanches.fichatecnica.infrastructure.config.security.DadosTokenJWT;
import com.fabriciosanches.fichatecnica.infrastructure.config.security.UsuarioSecurityDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SegurancaControllerTest {

    private MockMvc mockMvc;
    private RecuperacaoSenhaPort recuperacaoSenhaPort;
    private AuthenticationManager authenticationManager;
    private GerarTokenPort gerarTokenPort;
    private FichaTecnicaProperty fichaTecnicaProperty;

    @BeforeEach
    void setUp() {
        recuperacaoSenhaPort = Mockito.mock(RecuperacaoSenhaPort.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);
        gerarTokenPort = Mockito.mock(GerarTokenPort.class);
        fichaTecnicaProperty = new FichaTecnicaProperty();

        SegurancaController controller = new SegurancaController(
                recuperacaoSenhaPort,
                authenticationManager,
                gerarTokenPort,
                fichaTecnicaProperty
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void loginRecuperacaoSenha_DeveRetornar500QuandoCredenciaisNaoConfiguradas() throws Exception {
        fichaTecnicaProperty.getSystem().setUsername("");
        fichaTecnicaProperty.getSystem().setPassword("");

        mockMvc.perform(post("/ficha-tecnica/login-recuperacao-senha"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.jwt").value("Credenciais do usuÃ¡rio de sistema nÃ£o configuradas"));
    }

    @Test
    void loginRecuperacaoSenha_DeveRetornar403QuandoRoleNaoForSystem() throws Exception {
        fichaTecnicaProperty.getSystem().setUsername("system@email.com");
        fichaTecnicaProperty.getSystem().setPassword("123");

        Usuario usuario = new Usuario(1L, "system@email.com", "senha", UserRole.ADMIN, "Admin");
        UsuarioSecurityDetails details = new UsuarioSecurityDetails(usuario);
        Authentication authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        mockMvc.perform(post("/ficha-tecnica/login-recuperacao-senha"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.jwt").value("UsuÃ¡rio de sistema nÃ£o possui ROLE_SYSTEM"));
    }

    @Test
    void loginRecuperacaoSenha_DeveRetornar200QuandoAutenticarSystem() throws Exception {
        fichaTecnicaProperty.getSystem().setUsername("system@email.com");
        fichaTecnicaProperty.getSystem().setPassword("123");

        Usuario usuario = new Usuario(1L, "system@email.com", "senha", UserRole.SYSTEM, "Sistema");
        UsuarioSecurityDetails details = new UsuarioSecurityDetails(usuario);
        Authentication authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(gerarTokenPort.gerarToken(any(Usuario.class))).thenReturn(new DadosTokenJWT(
                "jwt-token",
                120L,
                OffsetDateTime.parse("2026-03-13T06:51:41.2063929-03:00"),
                "system@email.com",
                "Sistema",
                "ROLE_SYSTEM"
        ));

        mockMvc.perform(post("/ficha-tecnica/login-recuperacao-senha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("ROLE_SYSTEM"));
    }

    @Test
    void loginRecuperacaoSenha_DeveRetornar401QuandoCredenciaisInvalidas() throws Exception {
        fichaTecnicaProperty.getSystem().setUsername("system@email.com");
        fichaTecnicaProperty.getSystem().setPassword("123");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("invalid"));

        mockMvc.perform(post("/ficha-tecnica/login-recuperacao-senha"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.jwt").value("Credenciais do usuÃ¡rio de sistema invÃ¡lidas"));
    }

    @Test
    void enviarEmailSeguranca_DeveRetornarOk() throws Exception {
        when(recuperacaoSenhaPort.enviarEmailSeguranca(eq("user@email.com")))
                .thenReturn(new EnviarEmailSegurancaResponseDTO("user@email.com", "52998224725", "12345678", "01/01/2026 10:00:00"));

        mockMvc.perform(post("/ficha-tecnica/enviar-email-seguranca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"user@email.com\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@email.com"));
    }

    @Test
    void enviarEmailSeguranca_DeveRetornarBadRequestQuandoRegraNegocioFalhar() throws Exception {
        when(recuperacaoSenhaPort.enviarEmailSeguranca(eq("user@email.com")))
                .thenThrow(new FichaTecnicaException("erro"));

        mockMvc.perform(post("/ficha-tecnica/enviar-email-seguranca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"email\":\"user@email.com\"" +
                                "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void trocarSenha_DeveRetornarOk() throws Exception {
        mockMvc.perform(post("/ficha-tecnica/trocar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@email.com",
                                  "cpf": "52998224725",
                                  "tokenSeguranca": "12345678",
                                  "senha": "c2VuaGE=",
                                  "confirmacaoSenha": "c2VuaGE="
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void trocarSenha_DeveRetornarBadRequestQuandoServicoLancarExcecao() throws Exception {
        Mockito.doThrow(new FichaTecnicaException("erro"))
                .when(recuperacaoSenhaPort)
                .trocarSenhaSeguranca(any(), any(), any(), any(), any());

        mockMvc.perform(post("/ficha-tecnica/trocar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@email.com",
                                  "cpf": "52998224725",
                                  "tokenSeguranca": "12345678",
                                  "senha": "c2VuaGE=",
                                  "confirmacaoSenha": "c2VuaGE="
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("erro"));
    }
}


