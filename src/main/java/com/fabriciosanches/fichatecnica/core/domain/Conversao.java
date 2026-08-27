package com.fabriciosanches.fichatecnica.core.domain;

import java.math.BigDecimal;

public class Conversao {
    private Long codigo;
    private Long unidadeDe;
    private Long unidadePara;
    private String operacao;
    private BigDecimal valor;

    public Conversao() {
    }

    public Conversao(Long unidadeDe, Long unidadePara, String operacao, BigDecimal valor) {
        this(null, unidadeDe, unidadePara, operacao, valor);
    }

    public Conversao(Long codigo, Long unidadeDe, Long unidadePara, String operacao, BigDecimal valor) {
        this.codigo = codigo;
        this.unidadeDe = unidadeDe;
        this.unidadePara = unidadePara;
        this.operacao = operacao;
        this.valor = valor;
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public Long getUnidadeDe() {
        return unidadeDe;
    }

    public void setUnidadeDe(Long unidadeDe) {
        this.unidadeDe = unidadeDe;
    }

    public Long getUnidadePara() {
        return unidadePara;
    }

    public void setUnidadePara(Long unidadePara) {
        this.unidadePara = unidadePara;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
