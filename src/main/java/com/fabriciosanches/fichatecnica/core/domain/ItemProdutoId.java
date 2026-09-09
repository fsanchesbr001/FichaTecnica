package com.fabriciosanches.fichatecnica.core.domain;

import java.io.Serializable;
import java.util.Objects;

public class ItemProdutoId implements Serializable {
    private Long produtoId;
    private Long itemId;

    public ItemProdutoId() {
    }

    public ItemProdutoId(Long produtoId, Long itemId) {
        this.produtoId = produtoId;
        this.itemId = itemId;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemProdutoId that = (ItemProdutoId) o;
        return Objects.equals(produtoId, that.produtoId) && Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produtoId, itemId);
    }
}

