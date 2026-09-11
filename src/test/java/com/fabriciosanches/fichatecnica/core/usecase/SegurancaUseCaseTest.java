package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.constants.Constants;
import com.fabriciosanches.fichatecnica.core.domain.Seguranca;
import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.out.EnviarEmailPort;
import com.fabriciosanches.fichatecnica.core.ports.out.SegurancaRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UsuarioRepositoryPort;
import com.fabriciosanches.fichatecnica.dtos.EnviarEmailSegurancaResponseDTO;
import com.fabriciosanches.fichatecnica.enums.UserRole;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.util.Utilidades;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SegurancaUseCaseTest {

    @Mock
    private SegurancaRepositoryPort segurancaRepositoryPort;
    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;
    @Mock
    private EnviarEmailPort enviarEmailPort;

    @InjectMocks
    private SegurancaUseCase useCase;

    private Seguranca seguranca;

    @BeforeEach
    void setUp() {
        seguranca = new Seguranca();
        seguranca.setCodigo(1L);
        seguranca.setEmail("user@email.com");
        seguranca.setCpf("52998224725");
        seguranca.setTentativas(5);
        seguranca.setBloqueado_admin(false);
        seguranca.setBloqueado_tentativas(false);
        seguranca.setBloqueado_expiracao(false);
        seguranca.setPrimeiro_acesso(false);
        seguranca.setDataCriacao(LocalDateTime.now().minusDays(1));
        seguranca.setDataExpiracaoSenha(LocalDateTime.now().plusDays(30));
    }

    @Test
    void enviarEmailSeguranca_DeveGerarTokenESalvar() throws Exception {
        when(segurancaRepositoryPort.buscarCpfPorEmail("user@email.com")).thenReturn("52998224725");
        when(segurancaRepositoryPort.buscarPorEmail("user@email.com")).thenReturn(Optional.of(seguranca));

        EnviarEmailSegurancaResponseDTO response = useCase.enviarEmailSeguranca("user@email.com");

        assertEquals("user@email.com", response.email());
        assertTrue(response.tokenSeguranca().length() == 8);
        verify(enviarEmailPort).enviar(eq("user@email.com"), eq(Constants.SUBJECT_EMAIL_RECUPERACAO_SENHA), anyString());
    }

    @Test
    void validarAcesso_DeveLancarExcecaoQuandoBloqueadoAdm() {
        seguranca.setBloqueado_admin(true);
        when(segurancaRepositoryPort.buscarPorEmail("user@email.com")).thenReturn(Optional.of(seguranca));

        FichaTecnicaException ex = assertThrows(FichaTecnicaException.class,
                () -> useCase.validarAcesso("user@email.com"));

        assertEquals(Constants.MSG_ERRO_BLOQUEIO_ADM, ex.getMessage());
    }

    @Test
    void trocarSenhaSeguranca_DeveAtualizarSenhaDoUsuario() {
        String senhaBase64 = Utilidades.encodeToBase64("NovaSenha@123");
        Usuario usuario = new Usuario(1L, "user@email.com", "senha", UserRole.USER, "User");
        seguranca.setTokenSeguranca("12345678");
        seguranca.setDataExpiracaoToken(LocalDateTime.now().plusHours(1));

        when(segurancaRepositoryPort.buscarPorEmail("user@email.com")).thenReturn(Optional.of(seguranca));
        when(usuarioRepositoryPort.buscarPorLogin("user@email.com")).thenReturn(Optional.of(usuario));

        useCase.trocarSenhaSeguranca("user@email.com", "52998224725", "12345678", senhaBase64, senhaBase64);

        verify(usuarioRepositoryPort).salvar(usuario);
        assertTrue(usuario.getSenha() != null && !usuario.getSenha().equals("NovaSenha@123"));
    }
}

