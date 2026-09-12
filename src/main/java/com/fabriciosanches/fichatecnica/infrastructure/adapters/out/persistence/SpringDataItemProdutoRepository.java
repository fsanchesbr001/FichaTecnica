package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataItemProdutoRepository extends JpaRepository<ItemProdutoEntity, ItemProdutoIdEntity> {
    List<ItemProdutoEntity> findByProdutoCodigo(Long produtoCodigo);

    List<ItemProdutoEntity> findByItemCodigo(Long itemCodigo);

    Optional<ItemProdutoEntity> findByProdutoCodigoAndItemCodigo(Long produtoCodigo, Long itemCodigo);

    void deleteByProdutoCodigoAndItemCodigo(Long produtoCodigo, Long itemCodigo);
}

