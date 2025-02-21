package com.fabriciosanches.fichatecnica.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fabriciosanches.fichatecnica.usuario.Usuario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private final Logger logger = LogManager.getLogger(TokenService.class);

    public String gerarToken(Usuario usuario){
        logger.info("Inicio do método gerarToken");
        try {
            var algoritimo = Algorithm.HMAC256(secret);
            logger.info("Fim do método gerarToken");
            return JWT.create()
                    .withIssuer("API Contatos")
                    .withSubject(usuario.getLogin())
                    .withExpiresAt(dataExpiracao())
                    .sign(algoritimo);

        } catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar Token JWT",exception);
        }
    }

    public String getSubject(String tokenJWT){
        try {
            logger.info("Inicio do método getSubject");
            var algoritimo = Algorithm.HMAC256(secret);
            logger.info("Fim do método getSubject");
            return JWT.require(algoritimo)
                    .withIssuer("API Contatos")
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception){
            throw new RuntimeException("Verificação de Token falhou!!!",exception);
        }
    }

    private Instant dataExpiracao() {
        return LocalDateTime.now().plusMinutes(5).toInstant(ZoneOffset.of("-03:00"));
    }
}
