package com.fabriciosanches.fichatecnica.dtos;


public record EnviarEmailRequestDTO(String email)
{
    public EnviarEmailRequestDTO {
        if ( email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email não pode ser nulo ou vazio");
        }
    }
}
