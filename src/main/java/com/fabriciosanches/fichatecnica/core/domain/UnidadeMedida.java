package com.fabriciosanches.fichatecnica.core.domain;

public class UnidadeMedida {
    private Long codigo;
    private String nome;
    private String sigla;

    public UnidadeMedida(Long codigo,String nome,String sigla) {
        if (codigo == null || nome == null || sigla == null) {
            throw new IllegalArgumentException("Código, nome e sigla não podem ser nulos");
        }

        this.codigo = codigo;
        this.nome = nome;
        this.sigla = sigla;
    }

    private void validar(){
        if (nome==null || nome.isBlank() || sigla==null || sigla.isBlank()) {
            throw new IllegalArgumentException("Nome e sigla não podem ser vazios");
        }
    }
}
