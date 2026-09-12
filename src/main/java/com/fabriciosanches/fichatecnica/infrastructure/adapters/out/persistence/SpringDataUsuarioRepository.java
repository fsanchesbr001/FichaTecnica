package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByLogin(String login);

    @Query(value = "SELECT * FROM usuarios u WHERE u.login = :login", nativeQuery = true)
    Optional<UsuarioEntity> findByLoginUsuario(String login);
}


