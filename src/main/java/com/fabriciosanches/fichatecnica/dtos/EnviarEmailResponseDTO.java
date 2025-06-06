package com.fabriciosanches.fichatecnica.dtos;

public record EnviarEmailResponseDTO(String email, String cpf, String tokenSeguranca, String dataExpiracaoToken) {
    public EnviarEmailResponseDTO(SegurancaDTO segurancaDTO, String dataExpiracaoToken) {
        this(segurancaDTO.email(), segurancaDTO.cpf(), segurancaDTO.tokenSeguranca(),
                dataExpiracaoToken);
    }
}
