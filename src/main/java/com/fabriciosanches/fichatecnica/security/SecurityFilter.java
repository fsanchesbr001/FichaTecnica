package com.fabriciosanches.fichatecnica.security;

import com.fabriciosanches.fichatecnica.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final TokenService tokenService;
    private final UsuarioRepository repository;
    private final TokenBlacklistService tokenBlacklistService;

    public SecurityFilter(TokenService tokenService, UsuarioRepository repository,
                          TokenBlacklistService tokenBlacklistService) {
        this.tokenService = tokenService;
        this.repository = repository;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest  request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Preflight OPTIONS: deixar passar sem validar token para que o CORS funcione
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        var tokenJWT = recuperarToken(request);
        if(tokenJWT!=null){
            // Validar se o token está expirado
            if(!tokenService.validarTokenExpirado(tokenJWT)){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Token expirado");
                errorMap.put("message", "Seu token de autenticação expirou. Por favor, faça login novamente.");
                response.getWriter().write(new ObjectMapper().writeValueAsString(errorMap));
                return;
            }

            // Validar se o token foi revogado via logout
            if(tokenBlacklistService.estaRevogado(tokenJWT)){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Token revogado");
                errorMap.put("message", "Sua sessão foi encerrada. Por favor, faça login novamente.");
                response.getWriter().write(new ObjectMapper().writeValueAsString(errorMap));
                return;
            }

            try {
                var subject = tokenService.getSubject(tokenJWT);
                var role = tokenService.getRole(tokenJWT);
                if (role != null && !role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }
                var authority = new SimpleGrantedAuthority(role);
                var usuario = repository.findByLogin(subject);
                var authentication = new UsernamePasswordAuthenticationToken(usuario, tokenJWT,
                        Collections.singletonList(authority));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Token inválido");
                errorMap.put("message", "Seu token de autenticação é inválido. Por favor, faça login novamente.");
                response.getWriter().write(new ObjectMapper().writeValueAsString(errorMap));
                return;
            }
        }

        filterChain.doFilter(request,response);
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
