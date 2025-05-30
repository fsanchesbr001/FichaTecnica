package com.fabriciosanches.fichatecnica.dtos;

import java.time.LocalDateTime;

public record EnviarEmailResponse(String email, String cpf, String tokenSeguranca, LocalDateTime dataExpiracaoToken) {
    public EnviarEmailResponse(SegurancaDTO segurancaDTO){
        this(segurancaDTO.email(), segurancaDTO.cpf(), segurancaDTO.tokenSeguranca(),
                segurancaDTO.dataExpiracaoToken());
    }
}
