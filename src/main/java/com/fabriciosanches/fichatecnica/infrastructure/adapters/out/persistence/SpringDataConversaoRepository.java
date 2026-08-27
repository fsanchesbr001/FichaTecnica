package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataConversaoRepository extends JpaRepository<ConversaoEntity, Long> {
    @Query("SELECT COUNT(c) FROM Conversao c WHERE c.unidadeDe = :unidadeDe AND c.unidadePara = :unidadePara")
    long countByUnidadeDeAndUnidadePara(@Param("unidadeDe") Long unidadeDe, @Param("unidadePara") Long unidadePara);

    Optional<ConversaoEntity> findByUnidadeDeAndUnidadePara(Long unidadeDe, Long unidadePara);

    @Query("""
            SELECT new com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO(
                c.codigo, ud.nome, up.nome, c.operacao, c.valor)
            FROM Conversao c, UnidadeMedida ud, UnidadeMedida up
            WHERE ud.codigo = c.unidadeDe
              AND up.codigo = c.unidadePara
            ORDER BY ud.nome
            """)
    List<ConversaoRelatorioDTO> findAllComNomes();

    @Query("""
            SELECT new com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO(
                c.codigo, ud.nome, up.nome, c.operacao, c.valor)
            FROM Conversao c, UnidadeMedida ud, UnidadeMedida up
            WHERE ud.codigo = c.unidadeDe
              AND up.codigo = c.unidadePara
              AND c.codigo = :id
            """)
    Optional<ConversaoRelatorioDTO> findByIdComNomes(@Param("id") Long id);
}
