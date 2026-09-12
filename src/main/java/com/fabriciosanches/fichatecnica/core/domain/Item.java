package com.fabriciosanches.fichatecnica.core.domain;

import java.math.BigDecimal;
import java.util.List;

public class Item {
    private Long codigo;
    private String nome;
    private UnidadeMedida unidadeMedida;
    private BigDecimal valor;

    public Item() {
    }

    public Item(Long codigo, String nome, UnidadeMedida unidadeMedida, BigDecimal valor) {
        this.codigo = codigo;
        this.nome = nome;
        this.unidadeMedida = unidadeMedida;
        this.valor = valor;
    }

    public Item(Long codigo, String nome, UnidadeMedida unidadeMedida, List<?> ignoredItemProdutoList, BigDecimal valor) {
        this(codigo, nome, unidadeMedida, valor);
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

    public UnidadeMedida getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(UnidadeMedida unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}


