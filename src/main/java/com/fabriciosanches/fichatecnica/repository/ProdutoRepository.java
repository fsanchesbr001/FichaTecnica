package com.fabriciosanches.fichatecnica.repository;


import com.fabriciosanches.fichatecnica.domain.produto.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto,Long> {
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.nome = :nome")
    long countByName(@Param("nome") String nome);

}
