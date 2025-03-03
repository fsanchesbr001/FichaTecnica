package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemProdutoRepository extends JpaRepository<ItemProduto, Long> {
    List<ItemProduto> findByCdProduto(Long produtoId);
    List<ItemProduto> findByCdItem(Long itemId);
}
