package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;


public record BloqueiosRequestDTO(String email)
{
    public BloqueiosRequestDTO {
        if ( email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email nÃ£o pode ser nulo ou vazio");
        }
    }
}

