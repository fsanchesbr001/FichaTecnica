package com.fabriciosanches.fichatecnica.core.ports.out;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;

import java.util.Optional;

public interface UsuarioRepositoryPort {
    Optional<Usuario> buscarPorLogin(String login);

    Usuario salvar(Usuario usuario);

    void deletar(Usuario usuario);
}

