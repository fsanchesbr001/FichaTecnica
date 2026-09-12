package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.in.AutenticarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.out.GeradorTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UsuarioRepositoryPort;
import com.fabriciosanches.fichatecnica.enums.UserRole;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.config.security.DadosTokenJWT;

import java.util.Objects;

public class AutenticacaoUseCase implements AutenticarUsuarioPort, GerarTokenPort {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final GeradorTokenPort geradorTokenPort;

    public AutenticacaoUseCase(UsuarioRepositoryPort usuarioRepositoryPort, GeradorTokenPort geradorTokenPort) {
        this.usuarioRepositoryPort = Objects.requireNonNull(usuarioRepositoryPort, "UsuarioRepositoryPort não pode ser nulo");
        this.geradorTokenPort = Objects.requireNonNull(geradorTokenPort, "GeradorTokenPort não pode ser nulo");
    }

    @Override
    public Usuario buscarPorLogin(String username) {
        return usuarioRepositoryPort.buscarPorLogin(username)
                .orElseThrow(() -> new FichaTecnicaException("Usuário não encontrado: " + username));
    }

    @Override
    public DadosTokenJWT gerarToken(Usuario usuario) {
        String token = geradorTokenPort.gerarToken(usuario);
        return new DadosTokenJWT(
                token,
                geradorTokenPort.getExpirationMinutes(),
                geradorTokenPort.getTokenExpiresAt(),
                usuario.getLogin(),
                usuario.getNome(),
                usuario.getRole() != null ? usuario.getRole().getRole() : UserRole.USER.getRole()
        );
    }
}

