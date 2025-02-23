package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.UnidadeMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UnidadeMedidaRepository extends JpaRepository<UnidadeMedida,Long> {
    @Query("SELECT COUNT(u) FROM UnidadeMedida u WHERE u.nome = :nome")
    long countByName(@Param("nome") String nome);

}
