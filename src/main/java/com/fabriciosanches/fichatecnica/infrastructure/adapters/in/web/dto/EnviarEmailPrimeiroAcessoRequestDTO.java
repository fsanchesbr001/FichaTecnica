package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;


public record EnviarEmailPrimeiroAcessoRequestDTO(String email, String nomeUsuario, String senhaAleatoria)
{
    public EnviarEmailPrimeiroAcessoRequestDTO {
        if ( email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email nÃ£o pode ser nulo ou vazio");
        }

        if ( nomeUsuario == null || nomeUsuario.isBlank()) {
            throw new IllegalArgumentException("O nome do usuÃ¡rio  nÃ£o pode ser nulo ou vazio");
        }

        if ( senhaAleatoria == null || senhaAleatoria.isBlank()) {
            throw new IllegalArgumentException("A senha aleatÃ³ria nÃ£o pode ser nula ou vazia");
        }
    }
}

