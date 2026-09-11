package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.constants.Constants;
import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.in.ControleAcessoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarTokenPort;
import com.fabriciosanches.fichatecnica.dtos.AutenticacaoDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.security.DadosTokenJWT;
import com.fabriciosanches.fichatecnica.security.TokenBlacklistService;
import com.fabriciosanches.fichatecnica.security.TokenService;
import com.fabriciosanches.fichatecnica.security.UsuarioSecurityDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login, logout e emissão de JWT para acesso à API")
public class AutenticacaoController {

    private final AuthenticationManager manager;
    private final GerarTokenPort gerarTokenPort;
    private final ControleAcessoPort controleAcessoPort;
    private final TokenBlacklistService tokenBlacklistService;
    private final TokenService tokenService;

    public AutenticacaoController(AuthenticationManager manager,
                                  GerarTokenPort gerarTokenPort,
                                  ControleAcessoPort controleAcessoPort,
                                  TokenBlacklistService tokenBlacklistService,
                                  TokenService tokenService) {
        this.manager = manager;
        this.gerarTokenPort = gerarTokenPort;
        this.controleAcessoPort = controleAcessoPort;
        this.tokenBlacklistService = tokenBlacklistService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    @Operation(summary = "Efetua login", description = "Autentica o usuário e retorna o token JWT com dados de expiração e perfil.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Credenciais inválidas ou dados obrigatórios ausentes"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao autenticar")
    })
    public ResponseEntity<DadosTokenJWT> efetuarLogin(@RequestBody @Valid AutenticacaoDTO dados) {
        LoggerFactory.getLogger(this.getClass()).info("Fluxo entrou no método efetuarLogin - Usuário: {}", dados.login());

        if (dados.login() == null || dados.senha() == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            controleAcessoPort.validarAcesso(dados.login());
            var userPwd = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
            var authentication = manager.authenticate(userPwd);
            controleAcessoPort.resetarTentativas(dados.login());

            Usuario usuario = toUsuario((UserDetails) authentication.getPrincipal());
            return ResponseEntity.ok(gerarTokenPort.gerarToken(usuario));
        } catch (BadCredentialsException e) {
            controleAcessoPort.errouSenha(dados.login());
            return ResponseEntity.badRequest().body(new DadosTokenJWT(Constants.MSG_DADOS_SEGURANCA_NAO_ENCONTRADOS));
        } catch (FichaTecnicaException ex) {
            return ResponseEntity.badRequest().body(new DadosTokenJWT(ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new DadosTokenJWT(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Efetua logout", description = "Revoga o token JWT atual e encerra a sessão do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não há sessão ativa ou o token não pôde ser validado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> efetuarLogout() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getCredentials() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nenhuma sessão ativa encontrada"));
        }

        String token = (String) auth.getCredentials();
        Instant expiracao = tokenService.getExpiration(token);
        if (expiracao == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token inválido ou não foi possível determinar sua expiração"));
        }

        tokenBlacklistService.revogar(token, expiracao);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso. Sessão encerrada."));
    }

    private Usuario toUsuario(UserDetails userDetails) {
        if (userDetails instanceof UsuarioSecurityDetails details) {
            return details.getUsuario();
        }

        Usuario usuario = new Usuario();
        usuario.setLogin(userDetails.getUsername());
        usuario.setSenha(userDetails.getPassword());
        usuario.setNome(userDetails.getUsername());
        return usuario;
    }
}

