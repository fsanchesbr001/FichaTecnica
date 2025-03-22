package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemProdutoRepository extends JpaRepository<ItemProduto, Long> {
}
