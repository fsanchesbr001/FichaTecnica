package com.fabriciosanches.fichatecnica.infrastructure.config.security;

import com.fabriciosanches.fichatecnica.core.ports.in.AutenticarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.out.GerenciadorBlacklistTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ValidadorTokenPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(SecurityFilter.class);

    private final ValidadorTokenPort validadorTokenPort;
    private final AutenticarUsuarioPort autenticarUsuarioPort;
    private final GerenciadorBlacklistTokenPort gerenciadorBlacklistTokenPort;

    public SecurityFilter(ValidadorTokenPort validadorTokenPort, AutenticarUsuarioPort autenticarUsuarioPort,
                          GerenciadorBlacklistTokenPort gerenciadorBlacklistTokenPort) {
        this.validadorTokenPort = validadorTokenPort;
        this.autenticarUsuarioPort = autenticarUsuarioPort;
        this.gerenciadorBlacklistTokenPort = gerenciadorBlacklistTokenPort;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Preflight OPTIONS: deixar passar sem validar token para que o CORS funcione
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getMethod() + " " + request.getRequestURI();
        var tokenJWT = recuperarToken(request);

        if (tokenJWT == null) {
            log.warn("[SecurityFilter] {} - Nenhum token encontrado no header Authorization. Requisicao anonima.", uri);
            filterChain.doFilter(request, response);
            return;
        }

        // Validar se o token estÃ¡ expirado
        if (!validadorTokenPort.validarTokenExpirado(tokenJWT)) {
            log.warn("[SecurityFilter] {} - Token expirado.", uri);
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token expirado", "Seu token de autenticaÃ§Ã£o expirou. Por favor, faÃ§a login novamente.");
            return;
        }

        // Validar se o token foi revogado via logout
        if (gerenciadorBlacklistTokenPort.estaRevogado(tokenJWT)) {
            log.warn("[SecurityFilter] {} - Token revogado.", uri);
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token revogado", "Sua sessÃ£o foi encerrada. Por favor, faÃ§a login novamente.");
            return;
        }

        try {
            var subject = validadorTokenPort.getSubject(tokenJWT);
            var role = validadorTokenPort.getRole(tokenJWT);

            log.info("[SecurityFilter] {} - subject='{}' | role extraida do token='{}'", uri, subject, role);

            if (role == null || role.isBlank()) {
                log.error("[SecurityFilter] {} - Claim 'role' ausente ou vazia no token do usuario '{}'", uri, subject);
                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Token invÃ¡lido", "O token nÃ£o contÃ©m uma role vÃ¡lida. FaÃ§a login novamente.");
                return;
            }

            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }

            var authority = new SimpleGrantedAuthority(role);
            var usuario = autenticarUsuarioPort.buscarPorLogin(subject);
            var authentication = new UsernamePasswordAuthenticationToken(usuario, tokenJWT,
                    Collections.singletonList(authority));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("[SecurityFilter] {} - Autenticacao definida | authority='{}'", uri, role);

        } catch (Exception e) {
            log.error("[SecurityFilter] {} - Erro ao processar token: {}", uri, e.getMessage(), e);
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token invÃ¡lido", "Seu token de autenticaÃ§Ã£o Ã© invÃ¡lido. Por favor, faÃ§a login novamente.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeJsonError(HttpServletResponse response, int status, String error, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", error);
        errorMap.put("message", message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorMap));
    }

    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        var value = authorizationHeader.trim();
        if (value.regionMatches(true, 0, "Bearer", 0, "Bearer".length())) {
            value = value.substring("Bearer".length()).trim();
        }
        return value.isBlank() ? null : value;
    }
}


