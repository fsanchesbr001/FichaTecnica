package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.Conversao;
import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversaoRepository extends JpaRepository<Conversao,Long> {
    @Query("SELECT COUNT(u) FROM Conversao u WHERE u.unidadeDe = :unidadeDe AND u.unidadePara = :unidadePara")
    long countByUnidadeDeAndUnidadePara(@Param("unidadeDe") Long unidadeDe, @Param("unidadePara") Long unidadePara);

    Conversao findByUnidadeDeAndUnidadePara(Long unidadeDe, Long unidadePara);

    @Query("""
            SELECT new com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO(
                ud.nome, up.nome, c.operacao, c.valor)
            FROM Conversao c
            JOIN UnidadeMedida ud ON ud.codigo = c.unidadeDe
            JOIN UnidadeMedida up ON up.codigo = c.unidadePara
            ORDER BY ud.nome
            """)
    List<ConversaoRelatorioDTO> findAllComNomes();

    @Query("""
            SELECT new com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO(
                ud.nome, up.nome, c.operacao, c.valor)
            FROM Conversao c
            JOIN UnidadeMedida ud ON ud.codigo = c.unidadeDe
            JOIN UnidadeMedida up ON up.codigo = c.unidadePara
            WHERE c.codigo = :id
            """)
    Optional<ConversaoRelatorioDTO> findByIdComNomes(@Param("id") Long id);
}
