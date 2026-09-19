package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

import com.fabriciosanches.fichatecnica.core.domain.enums.UserRole;

public record RegisterDTO(String login, String senha, UserRole role, String nome, String cpf) {

    public RegisterDTO {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login não pode ser nulo ou vazio.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role não pode ser nula.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio.");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF não pode ser nulo ou vazio.");
        }
    }
}

