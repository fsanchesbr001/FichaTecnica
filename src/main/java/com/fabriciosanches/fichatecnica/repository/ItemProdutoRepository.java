package com.fabriciosanches.fichatecnica.repository;

import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemProdutoRepository extends JpaRepository<ItemProduto, Long> {
    List<ItemProduto> findByItem(ItemEntity item);
    List<ItemProduto> findByItemCodigo(Long itemCodigo);
    ItemProduto findByProdutoCodigoAndItemCodigo(Long idProduto, Long idItem);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM item_produto WHERE cd_item = ?1")
    void deleteItemProduto(@Param("cd_item") Long cdItem);

    List<ItemProduto> findByProdutoCodigo(Long idProduto);
}

