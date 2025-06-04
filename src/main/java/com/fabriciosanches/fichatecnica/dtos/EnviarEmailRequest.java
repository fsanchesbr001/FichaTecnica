package com.fabriciosanches.fichatecnica.dtos;


public record EnviarEmailRequest(String email)
{
    public EnviarEmailRequest {
        if ( email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email não pode ser nulo ou vazio");
        }
    }
}
