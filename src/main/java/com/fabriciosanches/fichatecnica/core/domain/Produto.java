package com.fabriciosanches.fichatecnica.core.domain;

import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;

import java.math.BigDecimal;
import java.util.List;


public class Produto {
    private Long codigo;
    private String nome;
    private String descricao;
    private String imagem;
    private BigDecimal valorVenda;
    private BigDecimal valorItens;
    private List<ItemProduto> produtosList;

    public Produto() {
    }

    public Produto(Long codigo, String nome, String descricao, String imagem, BigDecimal valorVenda, BigDecimal valorItens, List<ItemProduto> produtosList) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.imagem = imagem;
        this.valorVenda = valorVenda;
        this.valorItens = valorItens;
        this.produtosList = produtosList;
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public BigDecimal getValorVenda() {
        return valorVenda;
    }

    public void setValorVenda(BigDecimal valorVenda) {
        this.valorVenda = valorVenda;
    }

    public BigDecimal getValorItens() {
        return valorItens;
    }

    public void setValorItens(BigDecimal valorItens) {
        this.valorItens = valorItens;
    }

    public List<ItemProduto> getProdutosList() {
        return produtosList;
    }

    public void setProdutosList(List<ItemProduto> produtosList) {
        this.produtosList = produtosList;
    }
}


