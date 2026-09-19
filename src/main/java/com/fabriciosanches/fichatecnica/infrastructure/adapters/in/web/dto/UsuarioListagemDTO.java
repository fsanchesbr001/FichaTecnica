package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

import com.fabriciosanches.fichatecnica.core.domain.Seguranca;
import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.domain.enums.UserRole;

import java.time.LocalDateTime;

/**
 * DTO de listagem de usuários que combina dados da tabela {@code seguranca}
 * (controle de acesso) com os campos {@code nome} e {@code role} da tabela
 * {@code usuarios}.
 */
public record UsuarioListagemDTO(
        String email,
        String cpf,
        String tokenSeguranca,
        Integer tentativas,
        Boolean bloqueado_admin,
        Boolean bloqueado_tentativas,
        Boolean bloqueado_expiracao,
        Boolean primeiro_acesso,
        LocalDateTime dataCriacao,
        LocalDateTime dataExpiracaoSenha,
        LocalDateTime dataExpiracaoToken,
        String nome,
        String role
) {

    /**
     * Constrói o DTO combinando os dados de segurança com os dados do usuário.
     *
     * @param seguranca registro da tabela seguranca
     * @param usuario   registro da tabela usuarios (pode ser {@code null} quando não encontrado)
     */
    public UsuarioListagemDTO(Seguranca seguranca, Usuario usuario) {
        this(
                seguranca.getEmail(),
                seguranca.getCpf(),
                seguranca.getTokenSeguranca(),
                seguranca.getTentativas(),
                seguranca.getBloqueado_admin(),
                seguranca.getBloqueado_tentativas(),
                seguranca.getBloqueado_expiracao(),
                seguranca.getPrimeiro_acesso(),
                seguranca.getDataCriacao(),
                seguranca.getDataExpiracaoSenha(),
                seguranca.getDataExpiracaoToken(),
                usuario != null ? usuario.getNome() : null,
                usuario != null && usuario.getRole() != null ? usuario.getRole().name() : null
        );
    }
}


