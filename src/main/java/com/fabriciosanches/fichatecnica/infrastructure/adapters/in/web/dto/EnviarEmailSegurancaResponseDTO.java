package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

public record EnviarEmailSegurancaResponseDTO(String email, String cpf, String tokenSeguranca, String dataExpiracaoToken) {
    public EnviarEmailSegurancaResponseDTO(SegurancaDTO segurancaDTO, String dataExpiracaoToken) {
        this(segurancaDTO.email(), segurancaDTO.cpf(), segurancaDTO.tokenSeguranca(),
                dataExpiracaoToken);
    }
}

