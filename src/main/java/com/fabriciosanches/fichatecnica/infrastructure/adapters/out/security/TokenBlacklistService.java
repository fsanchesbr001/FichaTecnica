package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.security;

import com.fabriciosanches.fichatecnica.core.ports.out.GerenciadorBlacklistTokenPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiÃ§o responsÃ¡vel por manter a blacklist de tokens JWT invalidados via logout.
 * <p>
 * Como JWT Ã© stateless, o logout Ã© implementado adicionando o token a uma lista negra
 * em memÃ³ria. O token permanece na lista atÃ© atingir sua expiraÃ§Ã£o original, quando Ã©
 * removido automaticamente por uma tarefa agendada.
 * </p>
 */
@Service
public class TokenBlacklistService implements GerenciadorBlacklistTokenPort {

    private static final Logger logger = LogManager.getLogger(TokenBlacklistService.class);

    /**
     * Mapa de tokens invalidados: chave = token JWT, valor = instante de expiraÃ§Ã£o original.
     */
    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    /**
     * Adiciona um token Ã  blacklist atÃ© que expire.
     *
     * @param token     token JWT a ser invalidado
     * @param expiresAt instante de expiraÃ§Ã£o original do token
     */
    @Override
    public void revogar(String token, Instant expiresAt) {
        logger.info("Token adicionado a blacklist. Expira em: {}", expiresAt);
        blacklist.put(token, expiresAt);
    }

    /**
     * Verifica se um token estÃ¡ revogado (presente na blacklist).
     *
     * @param token token JWT a verificar
     * @return {@code true} se o token foi revogado via logout
     */
    @Override
    public boolean estaRevogado(String token) {
        return blacklist.containsKey(token);
    }

    /**
     * Tarefa agendada que limpa da blacklist os tokens que jÃ¡ expiraram naturalmente,
     * evitando crescimento ilimitado da estrutura em memÃ³ria.
     * Executada a cada 10 minutos.
     */
    @Override
    @Scheduled(fixedRateString = "${api.security.token.blacklist-cleanup-ms:600000}")
    public void limparTokensExpirados() {
        Instant agora = Instant.now();
        int antes = blacklist.size();
        blacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(agora));
        int removidos = antes - blacklist.size();
        if (removidos > 0) {
            logger.info("Blacklist: {} token(s) expirado(s) removido(s). Total atual: {}", removidos, blacklist.size());
        }
    }
}



