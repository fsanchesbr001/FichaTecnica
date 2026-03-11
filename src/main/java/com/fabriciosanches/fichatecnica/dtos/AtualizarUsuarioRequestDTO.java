package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.enums.UserRole;

/**
 * DTO de requisição para atualização parcial de um usuário.
 * Atualiza campos de controle de acesso na tabela {@code seguranca}
 * e os campos {@code nome} e {@code role} na tabela {@code usuarios}.
 *
 * @param bloqueado_admin      indica se o usuário está bloqueado administrativamente
 * @param bloqueado_tentativas indica se o usuário está bloqueado por excesso de tentativas
 * @param bloqueado_expiracao  indica se o usuário está bloqueado por expiração de senha
 * @param primeiro_acesso      indica se é o primeiro acesso do usuário
 * @param nome                 nome completo do usuário
 * @param role                 perfil de acesso do usuário
 */
public record AtualizarUsuarioRequestDTO(
        Boolean bloqueado_admin,
        Boolean bloqueado_tentativas,
        Boolean bloqueado_expiracao,
        Boolean primeiro_acesso,
        String nome,
        UserRole role
) {
}

