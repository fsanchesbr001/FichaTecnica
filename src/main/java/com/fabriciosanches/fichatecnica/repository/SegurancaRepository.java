package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.Seguranca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SegurancaRepository extends JpaRepository<Seguranca,Long> {
    @Query("SELECT p.cpf FROM Seguranca p WHERE p.email = :email")
    String findCPFByEmail(@Param("email") String email);

    Seguranca findByEmail(String email);

}
