package com.fabriciosanches.fichatecnica.dtos;


public record BloqueiosRequestDTO(String email)
{
    public BloqueiosRequestDTO {
        if ( email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email não pode ser nulo ou vazio");
        }
    }
}
