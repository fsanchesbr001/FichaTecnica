package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository repository;

    public UsuarioPersistenceAdapter(SpringDataUsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return repository.findByLogin(login).map(this::toDomain);
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioEntity salvo = repository.save(toEntity(usuario));
        return toDomain(salvo);
    }

    @Override
    public void deletar(Usuario usuario) {
        repository.delete(toEntity(usuario));
    }

    private Usuario toDomain(UsuarioEntity entity) {
        return new Usuario(entity.getId(), entity.getLogin(), entity.getSenha(), entity.getRole(), entity.getNome());
    }

    private UsuarioEntity toEntity(Usuario domain) {
        return new UsuarioEntity(domain.getId(), domain.getLogin(), domain.getSenha(), domain.getRole(), domain.getNome());
    }
}


