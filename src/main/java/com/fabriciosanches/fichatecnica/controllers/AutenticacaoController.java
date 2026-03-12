package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.constants.Constants;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.UsuarioRepository;
import com.fabriciosanches.fichatecnica.security.DadosTokenJWT;
import com.fabriciosanches.fichatecnica.security.TokenBlacklistService;
import com.fabriciosanches.fichatecnica.security.TokenService;
import com.fabriciosanches.fichatecnica.dtos.AutenticacaoDTO;
import com.fabriciosanches.fichatecnica.domains.Usuario;
import com.fabriciosanches.fichatecnica.services.SegurancaService;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    private final AuthenticationManager manager;
    private final TokenService tokenService;
    private final SegurancaService segurancaService;
    private final TokenBlacklistService tokenBlacklistService;

    public AutenticacaoController(AuthenticationManager manager,
                                  TokenService tokenService,
                                  UsuarioRepository usuarioRepository,
                                  SegurancaService segurancaService,
                                  TokenBlacklistService tokenBlacklistService) {
        this.manager = manager;
        this.tokenService = tokenService;
        this.segurancaService = segurancaService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    //Testado
    @PostMapping("/login")
    public ResponseEntity<DadosTokenJWT> efetuarLogin(@RequestBody @Valid AutenticacaoDTO dados){
        LoggerFactory.getLogger(this.getClass()).info("Fluxo entrou no método efetuarLogin - Usuário: {}", dados.login());

        if(dados.login() == null || dados.senha() == null){
            return ResponseEntity.badRequest().build();
        }
        try{
            segurancaService.validarAcesso(dados.login());
            var userPwd = new UsernamePasswordAuthenticationToken(dados.login(),dados.senha());
            var authentication = manager.authenticate(userPwd);
            segurancaService.resetarTentativas(dados.login());

            var usuario = (Usuario) authentication.getPrincipal();
            var token = tokenService.gerarToken(usuario);
            var expirationMinutes = tokenService.getExpirationMinutes();
            var expiresAt = tokenService.getTokenExpiresAt();
            var role = usuario.getRole().getRole();

            return ResponseEntity.ok(new DadosTokenJWT(
                token,
                expirationMinutes,
                expiresAt,
                usuario.getLogin(),
                usuario.getNome(),
                role
            ));
        }catch (BadCredentialsException e){
            segurancaService.errouSenha(dados.login());
            return ResponseEntity.badRequest().body(new DadosTokenJWT(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS));
        }
        catch (FichaTecnicaException ex) {
            return ResponseEntity.badRequest().body(new DadosTokenJWT(ex.getMessage()));
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(new DadosTokenJWT(e.getMessage()));
        }
    }

    /**
     * Encerra a sessão do usuário invalidando o JWT Token atual.
     * <p>
     * O token é recuperado das credenciais do {@link SecurityContextHolder} (armazenado
     * pelo {@code SecurityFilter}) e adicionado a uma blacklist em memória. Todas as
     * requisições subsequentes com esse token serão rejeitadas com 401.
     * </p>
     *
     * @return 200 OK com mensagem de confirmação, ou 400 se não houver sessão ativa
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> efetuarLogout() {
        LoggerFactory.getLogger(this.getClass()).info("Fluxo entrou no método efetuarLogout");

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getCredentials() == null) {
            LoggerFactory.getLogger(this.getClass()).warn("Logout chamado sem sessão ativa no SecurityContext");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Nenhuma sessão ativa encontrada"));
        }

        String token = (String) auth.getCredentials();

        Instant expiracao = tokenService.getExpiration(token);
        if (expiracao == null) {
            LoggerFactory.getLogger(this.getClass()).warn("Logout: não foi possível determinar a expiração do token");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token inválido ou não foi possível determinar sua expiração"));
        }

        tokenBlacklistService.revogar(token, expiracao);
        SecurityContextHolder.clearContext();

        LoggerFactory.getLogger(this.getClass()).info("Logout realizado com sucesso. Token revogado até: {}", expiracao);

        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso. Sessão encerrada."));
    }
}
