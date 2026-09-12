package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;


public record EnviarEmailRequestDTO(String email)
{
    public EnviarEmailRequestDTO {
        if ( email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email nÃ£o pode ser nulo ou vazio");
        }
    }
}

