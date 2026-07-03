package com.fabriciosanches.fichatecnica.core.domain;

public class UnidadeMedida {
    private final Long codigo;
    private final String nome;
    private final String sigla;

    public UnidadeMedida(String nome, String sigla) {
        this(null, nome, sigla);
    }

    public UnidadeMedida(Long codigo, String nome, String sigla) {
        this.codigo = codigo;
        this.nome = validarCampo(nome, "Nome não pode ser vazio");
        this.sigla = validarCampo(sigla, "Sigla não pode ser vazia");
    }

    private String validarCampo(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }

    public Long getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }
}
