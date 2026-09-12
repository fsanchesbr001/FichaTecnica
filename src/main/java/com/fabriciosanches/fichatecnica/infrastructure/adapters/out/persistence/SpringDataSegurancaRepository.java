package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataSegurancaRepository extends JpaRepository<SegurancaEntity, Long> {
    @Query("SELECT p.cpf FROM Seguranca p WHERE p.email = :email")
    String findCPFByEmail(@Param("email") String email);

    Optional<SegurancaEntity> findByEmail(String email);
}


