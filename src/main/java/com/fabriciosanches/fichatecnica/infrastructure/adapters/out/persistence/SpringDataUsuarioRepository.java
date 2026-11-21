package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByLogin(String login);

    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = "SELECT * FROM usuarios WHERE login = :login FOR UPDATE NOWAIT", nativeQuery = true)
    Optional<UsuarioEntity> findForUpdateByLogin(@Param("login") String login);

    @Query(value = "SELECT * FROM usuarios u WHERE u.login = :login", nativeQuery = true)
    Optional<UsuarioEntity> findByLoginUsuario(String login);
}


