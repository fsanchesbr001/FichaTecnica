package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

import com.fabriciosanches.fichatecnica.core.domain.enums.UserRole;

/**
 * DTO de requisiÃ§Ã£o para atualizaÃ§Ã£o parcial de um usuÃ¡rio.
 * Atualiza campos de controle de acesso na tabela {@code seguranca}
 * e os campos {@code nome} e {@code role} na tabela {@code usuarios}.
 *
 * @param bloqueado_admin      indica se o usuÃ¡rio estÃ¡ bloqueado administrativamente
 * @param bloqueado_tentativas indica se o usuÃ¡rio estÃ¡ bloqueado por excesso de tentativas
 * @param bloqueado_expiracao  indica se o usuÃ¡rio estÃ¡ bloqueado por expiraÃ§Ã£o de senha
 * @param primeiro_acesso      indica se Ã© o primeiro acesso do usuÃ¡rio
 * @param nome                 nome completo do usuÃ¡rio
 * @param role                 perfil de acesso do usuÃ¡rio
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


