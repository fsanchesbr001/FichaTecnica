package com.fabriciosanches.fichatecnica.repository;


import com.fabriciosanches.fichatecnica.domains.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto,Long> {
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.nome = :nome")
    long countByName(@Param("nome") String nome);

    @Query("SELECT p FROM Produto p JOIN p.itens ip WHERE ip.item.codigo= :cdItem")
    List<Produto> findByCdItem(@Param("cdItem") Long cdItem);
}
