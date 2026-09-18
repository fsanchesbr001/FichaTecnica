package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.infrastructure.constants.Constants;
import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.in.ControleAcessoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.out.GerenciadorBlacklistTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ValidadorTokenPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.AutenticacaoDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.config.security.DadosTokenJWT;
import com.fabriciosanches.fichatecnica.infrastructure.config.security.UsuarioSecurityDetails;
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
@Tag(name = "AutenticaÃ§Ã£o", description = "Login, logout e emissÃ£o de JWT para acesso Ã  API")
public class AutenticacaoController {

    private final AuthenticationManager manager;
    private final GerarTokenPort gerarTokenPort;
    private final ControleAcessoPort controleAcessoPort;
    private final GerenciadorBlacklistTokenPort gerenciadorBlacklistTokenPort;
    private final ValidadorTokenPort validadorTokenPort;

    public AutenticacaoController(AuthenticationManager manager,
                                  GerarTokenPort gerarTokenPort,
                                  ControleAcessoPort controleAcessoPort,
                                  GerenciadorBlacklistTokenPort gerenciadorBlacklistTokenPort,
                                  ValidadorTokenPort validadorTokenPort) {
        this.manager = manager;
        this.gerarTokenPort = gerarTokenPort;
        this.controleAcessoPort = controleAcessoPort;
        this.gerenciadorBlacklistTokenPort = gerenciadorBlacklistTokenPort;
        this.validadorTokenPort = validadorTokenPort;
    }

    @PostMapping("/login")
    @Operation(summary = "Efetua login", description = "Autentica o usuÃ¡rio e retorna o token JWT com dados de expiraÃ§Ã£o e perfil.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Credenciais invÃ¡lidas ou dados obrigatÃ³rios ausentes"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao autenticar")
    })
    public ResponseEntity<DadosTokenJWT> efetuarLogin(@RequestBody @Valid AutenticacaoDTO dados) {
        LoggerFactory.getLogger(this.getClass()).info("Fluxo entrou no metodo efetuarLogin - Usuario: {}", dados.login());

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
    @Operation(summary = "Efetua logout", description = "Revoga o token JWT atual e encerra a sessÃ£o do usuÃ¡rio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "NÃ£o hÃ¡ sessÃ£o ativa ou o token nÃ£o pÃ´de ser validado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou invÃ¡lido")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> efetuarLogout() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getCredentials() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nenhuma sessÃ£o ativa encontrada"));
        }

        String token = (String) auth.getCredentials();
        Instant expiracao = validadorTokenPort.getExpiration(token);
        if (expiracao == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token invÃ¡lido ou nÃ£o foi possÃ­vel determinar sua expiraÃ§Ã£o"));
        }

        gerenciadorBlacklistTokenPort.revogar(token, expiracao);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso. SessÃ£o encerrada."));
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


