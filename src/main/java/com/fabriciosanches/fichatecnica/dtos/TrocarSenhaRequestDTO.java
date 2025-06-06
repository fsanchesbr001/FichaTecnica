package com.fabriciosanches.fichatecnica.dtos;

public record TrocarSenhaRequestDTO(String email, String cpf, String tokenSeguranca,
                                    String senha, String confirmacaoSenha) {
    public TrocarSenhaRequestDTO {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email não pode ser nulo ou vazio");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("O CPF não pode ser nulo ou vazio");
        }
        if (tokenSeguranca == null || tokenSeguranca.isBlank()) {
            throw new IllegalArgumentException("O token de segurança não pode ser nulo ou vazio");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha não pode ser nula ou vazia");
        }
        if (confirmacaoSenha == null || confirmacaoSenha.isBlank()) {
            throw new IllegalArgumentException("A confirmação da senha não pode ser nula ou vazia");
        }
    }
}
