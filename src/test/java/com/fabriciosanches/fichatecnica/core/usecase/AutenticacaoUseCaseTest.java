package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.out.GeradorTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UsuarioRepositoryPort;
import com.fabriciosanches.fichatecnica.core.domain.enums.UserRole;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.config.security.DadosTokenJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoUseCaseTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private GeradorTokenPort geradorTokenPort;

    @Test
    void buscarPorLogin_DeveRetornarUsuario() {
        AutenticacaoUseCase useCase = new AutenticacaoUseCase(usuarioRepositoryPort, geradorTokenPort);
        Usuario usuario = new Usuario(1L, "admin@email.com", "senha", UserRole.ADMIN, "Admin");
        when(usuarioRepositoryPort.buscarPorLogin("admin@email.com")).thenReturn(Optional.of(usuario));

        Usuario result = useCase.buscarPorLogin("admin@email.com");

        assertEquals("admin@email.com", result.getLogin());
        assertEquals("Admin", result.getNome());
    }

    @Test
    void buscarPorLogin_DeveLancarExcecaoQuandoNaoEncontrar() {
        AutenticacaoUseCase useCase = new AutenticacaoUseCase(usuarioRepositoryPort, geradorTokenPort);
        when(usuarioRepositoryPort.buscarPorLogin("inexistente@email.com")).thenReturn(Optional.empty());

        assertThrows(FichaTecnicaException.class, () -> useCase.buscarPorLogin("inexistente@email.com"));
    }

    @Test
    void gerarToken_DeveMontarDadosCompletos() {
        AutenticacaoUseCase useCase = new AutenticacaoUseCase(usuarioRepositoryPort, geradorTokenPort);
        Usuario usuario = new Usuario(1L, "admin@email.com", "senha", UserRole.ADMIN, "Admin");
        OffsetDateTime expiraEm = OffsetDateTime.parse("2026-05-28T01:00:00-03:00");

        when(geradorTokenPort.gerarToken(usuario)).thenReturn("jwt");
        when(geradorTokenPort.getExpirationMinutes()).thenReturn(120L);
        when(geradorTokenPort.getTokenExpiresAt()).thenReturn(expiraEm);

        DadosTokenJWT result = useCase.gerarToken(usuario);

        assertEquals("jwt", result.jwt());
        assertEquals(120L, result.expirationMinutes());
        assertEquals(expiraEm, result.expiresAt());
        assertEquals("admin@email.com", result.usuarioLogin());
        assertEquals("Admin", result.usuarioNome());
        assertEquals("ROLE_ADMIN", result.role());
    }
}

