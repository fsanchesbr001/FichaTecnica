package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemProdutoRepository extends JpaRepository<ItemProduto, Long> {
    @Query("SELECT ip FROM ItemProduto ip WHERE ip.produto.codigo = :cdProduto")
    List<ItemProduto> findByCdProduto(@Param("cdProduto") Long cdProduto);
}
