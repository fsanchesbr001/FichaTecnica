package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.out.GeradorTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ValidadorTokenPort;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TokenService implements GeradorTokenPort, ValidadorTokenPort {

    @Value("${api.security.token.secret}")
    private String secret;

    @Getter
    @Value("${api.security.token.expiration-minutes:120}")
    private long expirationMinutes;

    @Value("${api.security.token.time-zone:America/Sao_Paulo}")
    private String tokenTimeZone;

    private final Logger logger = LogManager.getLogger(TokenService.class);

    @Override
    public String gerarToken(Usuario usuario){
        logger.info("Inicio do mÃ©todo gerarToken");
        try {
            var algoritimo = Algorithm.HMAC256(secret);
            logger.info("Fim do mÃ©todo gerarToken");
            return JWT.create()
                    .withIssuer("API Ficha Tecnica")
                    .withSubject(usuario.getLogin())
                    .withClaim("role", usuario.getRole().getRole())
                    .withClaim("nome", usuario.getNome())
                    .withExpiresAt(dataExpiracao())
                    .sign(algoritimo);

        } catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar Token JWT",exception);
        }
    }

    @Override
    public String getSubject(String tokenJWT){
        try {
            logger.info("Inicio do mÃ©todo getSubject");
            var algoritimo = Algorithm.HMAC256(secret);
            logger.info("Fim do mÃ©todo getSubject");
            return JWT.require(algoritimo)
                    .withIssuer("API Ficha Tecnica")
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception){
            throw new RuntimeException("VerificaÃ§Ã£o de Token falhou!!!",exception);
        }
    }

    @Override
    public String getRole(String tokenJWT){
        try {
            logger.info("Inicio do mÃ©todo getRole");
            var algoritimo = Algorithm.HMAC256(secret);
            logger.info("Fim do mÃ©todo getRole");
            return JWT.require(algoritimo)
                    .withIssuer("API Ficha Tecnica")
                    .build()
                    .verify(tokenJWT)
                    .getClaim("role").asString();
        } catch (JWTVerificationException exception){
            throw new RuntimeException("VerificaÃ§Ã£o de Token falhou!!!",exception);
        }
    }

    @Override
    public OffsetDateTime getTokenExpiresAt() {
        return dataExpiracao().atZone(getTokenZoneId()).toOffsetDateTime();
    }

    private Instant dataExpiracao() {
        return ZonedDateTime.now(getTokenZoneId())
                .plusMinutes(expirationMinutes)
                .toInstant();
    }

    private ZoneId getTokenZoneId() {
        return ZoneId.of(tokenTimeZone);
    }

    /**
     * Extrai o instante de expiraÃ§Ã£o de um token JWT jÃ¡ emitido.
     *
     * @param tokenJWT token JWT
     * @return instante de expiraÃ§Ã£o ou {@code null} se nÃ£o for possÃ­vel extrair
     */
    @Override
    public Instant getExpiration(String tokenJWT) {
        try {
            var algoritimo = Algorithm.HMAC256(secret);
            var decoded = JWT.require(algoritimo)
                    .withIssuer("API Ficha Tecnica")
                    .build()
                    .verify(tokenJWT);
            return decoded.getExpiresAtAsInstant();
        } catch (JWTVerificationException exception) {
            logger.warn("NÃ£o foi possÃ­vel extrair expiraÃ§Ã£o do token: {}", exception.getMessage());
            return null;
        }
    }

    @Override
    public boolean validarTokenExpirado(String tokenJWT) {
        try {
            logger.info("Validando expiraÃ§Ã£o do token");
            var algoritimo = Algorithm.HMAC256(secret);
            JWT.require(algoritimo)
                    .withIssuer("API Ficha Tecnica")
                    .build()
                    .verify(tokenJWT);
            logger.info("Token vÃ¡lido e nÃ£o expirado");
            return true;
        } catch (TokenExpiredException exception) {
            logger.warn("Token expirado: {}", exception.getMessage());
            return false;
        } catch (JWTVerificationException exception) {
            logger.warn("Erro ao verificar token: {}", exception.getMessage());
            return false;
        }
    }
}


