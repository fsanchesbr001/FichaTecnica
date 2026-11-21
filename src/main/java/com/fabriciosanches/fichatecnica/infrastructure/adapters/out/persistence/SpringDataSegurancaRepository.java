package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface SpringDataSegurancaRepository extends JpaRepository<SegurancaEntity, Long> {
    @Query("SELECT p.cpf FROM Seguranca p WHERE p.email = :email")
    String findCPFByEmail(@Param("email") String email);

    Optional<SegurancaEntity> findByEmail(String email);

    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = "SELECT * FROM seguranca WHERE email = :email FOR UPDATE NOWAIT", nativeQuery = true)
    Optional<SegurancaEntity> findForUpdateByEmail(@Param("email") String email);
}


