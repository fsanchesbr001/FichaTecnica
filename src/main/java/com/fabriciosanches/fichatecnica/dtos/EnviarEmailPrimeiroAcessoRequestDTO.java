package com.fabriciosanches.fichatecnica.dtos;


public record EnviarEmailPrimeiroAcessoRequestDTO(String email, String nomeUsuario, String senhaAleatoria)
{
    public EnviarEmailPrimeiroAcessoRequestDTO {
        if ( email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email não pode ser nulo ou vazio");
        }

        if ( nomeUsuario == null || nomeUsuario.isBlank()) {
            throw new IllegalArgumentException("O nome do usuário  não pode ser nulo ou vazio");
        }

        if ( senhaAleatoria == null || senhaAleatoria.isBlank()) {
            throw new IllegalArgumentException("A senha aleatória não pode ser nula ou vazia");
        }
    }
}
