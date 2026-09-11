package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.EnviarEmailPrimeiroAcessoRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.EnviarEmailSegurancaResponseDTO;
import jakarta.mail.MessagingException;

public interface RecuperacaoSenhaPort {
    EnviarEmailSegurancaResponseDTO enviarEmailSeguranca(String email) throws MessagingException;

    void enviarEmailPrimeiroAcesso(EnviarEmailPrimeiroAcessoRequestDTO dados) throws MessagingException;

    void trocarSenhaSeguranca(String email, String cpf, String tokenSeguranca, String senha, String confirmacaoSenha);
}

