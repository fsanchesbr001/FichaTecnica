package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

public record TrocarSenhaRequestDTO(String email, String cpf, String tokenSeguranca,
                                    String senha, String confirmacaoSenha) {
    public TrocarSenhaRequestDTO {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email nÃ£o pode ser nulo ou vazio");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("O CPF nÃ£o pode ser nulo ou vazio");
        }
        if (tokenSeguranca == null || tokenSeguranca.isBlank()) {
            throw new IllegalArgumentException("O token de seguranÃ§a nÃ£o pode ser nulo ou vazio");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha nÃ£o pode ser nula ou vazia");
        }
        if (confirmacaoSenha == null || confirmacaoSenha.isBlank()) {
            throw new IllegalArgumentException("A confirmaÃ§Ã£o da senha nÃ£o pode ser nula ou vazia");
        }
    }
}

