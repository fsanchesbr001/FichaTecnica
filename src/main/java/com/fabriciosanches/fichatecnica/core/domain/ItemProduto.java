package com.fabriciosanches.fichatecnica.core.domain;

import java.math.BigDecimal;

public class ItemProduto {
    private ItemProdutoId id;
    private Item item;
    private Produto produto;
    private UnidadeMedida unidadePara;
    private Double quantidade;
    private BigDecimal valor;

    public ItemProduto() {
    }

    public ItemProduto(ItemProdutoId id, Item item, Produto produto, UnidadeMedida unidadePara, Double quantidade, BigDecimal valor) {
        this.id = id;
        this.item = item;
        this.produto = produto;
        this.unidadePara = unidadePara;
        this.quantidade = quantidade;
        this.valor = valor;
    }

    public ItemProdutoId getId() {
        return id;
    }

    public void setId(ItemProdutoId id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public UnidadeMedida getUnidadePara() {
        return unidadePara;
    }

    public void setUnidadePara(UnidadeMedida unidadePara) {
        this.unidadePara = unidadePara;
    }

    public Double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Double quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}

