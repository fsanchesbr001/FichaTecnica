package com.fabriciosanches.fichatecnica.core.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HistoricoItem {
    private Long codigo;
    private Long cdItem;
    private BigDecimal valor;
    private LocalDate dataInicio;

    public HistoricoItem() {
    }

    public HistoricoItem(Long codigo, Long cdItem, BigDecimal valor, LocalDate dataInicio) {
        this.codigo = codigo;
        this.cdItem = cdItem;
        this.valor = valor;
        this.dataInicio = dataInicio;
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public Long getCdItem() {
        return cdItem;
    }

    public void setCdItem(Long cdItem) {
        this.cdItem = cdItem;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }
}

