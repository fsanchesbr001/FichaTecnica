package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.domains.Seguranca;

import java.time.LocalDateTime;

public record SegurancaDTO(String email, String cpf, String tokenSeguranca,
                           Integer tentativas, Boolean bloqueado_admin, Boolean bloqueado_tentativas,
                           Boolean bloqueado_expiracao, Boolean primeiro_acesso, LocalDateTime dataCriacao,
                           LocalDateTime dataExpiracaoSenha, LocalDateTime dataExpiracaoToken) {

    public SegurancaDTO(Seguranca seguranca) {
        this(seguranca.getEmail(), seguranca.getCpf(), seguranca.getTokenSeguranca(),
             seguranca.getTentativas(), seguranca.getBloqueado_admin(), seguranca.getBloqueado_tentativas(),
             seguranca.getBloqueado_expiracao(), seguranca.getPrimeiro_acesso(), seguranca.getDataCriacao(),
             seguranca.getDataExpiracaoSenha(), seguranca.getDataExpiracaoToken());

    }
}
