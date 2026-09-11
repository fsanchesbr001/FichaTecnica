package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.out.UsuarioRepositoryPort;
import com.fabriciosanches.fichatecnica.enums.UserRole;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.security.DadosTokenJWT;
import com.fabriciosanches.fichatecnica.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private TokenService tokenService;

    @InjectMocks
    private AutenticacaoUseCase useCase;

    @Test
    void buscarPorLogin_DeveRetornarUsuario() {
        Usuario usuario = new Usuario(1L, "admin@email.com", "senha", UserRole.ADMIN, "Admin");
        when(usuarioRepositoryPort.buscarPorLogin("admin@email.com")).thenReturn(Optional.of(usuario));

        Usuario result = useCase.buscarPorLogin("admin@email.com");

        assertEquals("admin@email.com", result.getLogin());
        assertEquals("Admin", result.getNome());
    }

    @Test
    void buscarPorLogin_DeveLancarExcecaoQuandoNaoEncontrar() {
        when(usuarioRepositoryPort.buscarPorLogin("inexistente@email.com")).thenReturn(Optional.empty());

        assertThrows(FichaTecnicaException.class, () -> useCase.buscarPorLogin("inexistente@email.com"));
    }

    @Test
    void gerarToken_DeveMontarDadosCompletos() {
        Usuario usuario = new Usuario(1L, "admin@email.com", "senha", UserRole.ADMIN, "Admin");

        when(tokenService.gerarToken(usuario)).thenReturn("jwt");
        when(tokenService.getExpirationMinutes()).thenReturn(120L);
        when(tokenService.getTokenExpiresAt()).thenReturn(OffsetDateTime.parse("2026-05-28T01:00:00-03:00"));

        DadosTokenJWT dados = useCase.gerarToken(usuario);

        assertEquals("jwt", dados.jwt());
        assertEquals("admin@email.com", dados.usuarioLogin());
        assertEquals("ROLE_ADMIN", dados.role());
    }
}
