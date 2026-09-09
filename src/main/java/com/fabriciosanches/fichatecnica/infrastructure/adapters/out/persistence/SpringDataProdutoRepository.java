package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataProdutoRepository extends JpaRepository<ProdutoEntity, Long> {
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.nome = :nome")
    long countByName(@Param("nome") String nome);
}
