package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domain.conversao.Conversao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversaoRepository extends JpaRepository<Conversao,Long> {
    @Query("SELECT COUNT(u) FROM Conversao u WHERE u.unidadeDe = :unidadeDe AND u.unidadePara = :unidadePara")
    long countByUnidadeDeAndUnidadePara(@Param("unidadeDe") Long unidadeDe, @Param("unidadePara") Long unidadePara);

}
