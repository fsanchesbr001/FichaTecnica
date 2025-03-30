package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemProdutoRepository extends JpaRepository<ItemProduto, Long> {
    List<ItemProduto> findByItem(Item item);
    ItemProduto findByProdutoCodigoAndItemCodigo(Long idProduto, Long idItem);
}
