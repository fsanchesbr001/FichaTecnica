package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataUnidadeMedidaRepository extends JpaRepository<UnidadeMedidaEntity, Long> {
    @Query("SELECT COUNT(u) FROM UnidadeMedida u WHERE u.nome = :nome")
    long countByName(@Param("nome") String nome);

    @Query("SELECT COUNT(u) FROM UnidadeMedida u WHERE u.sigla = :sigla")
    long countBySigla(@Param("sigla") String sigla);

    @Query("SELECT COUNT(u) FROM UnidadeMedida u WHERE u.sigla = :sigla AND u.codigo <> :codigo")
    long countBySiglaAndCodigoNot(@Param("sigla") String sigla, @Param("codigo") Long codigo);

    Optional<UnidadeMedidaEntity> findBySigla(String sigla);
}
