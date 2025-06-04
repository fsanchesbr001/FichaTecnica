package com.fabriciosanches.fichatecnica.dtos;

public record EnviarEmailResponse(String email, String cpf, String tokenSeguranca, String dataExpiracaoToken) {
    public EnviarEmailResponse(SegurancaDTO segurancaDTO, String dataExpiracaoToken) {
        this(segurancaDTO.email(), segurancaDTO.cpf(), segurancaDTO.tokenSeguranca(),
                dataExpiracaoToken);
    }
}
