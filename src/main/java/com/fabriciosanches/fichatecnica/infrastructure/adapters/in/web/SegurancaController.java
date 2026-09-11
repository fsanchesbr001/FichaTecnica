package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RecuperacaoSenhaPort;
import com.fabriciosanches.fichatecnica.dtos.EnviarEmailPrimeiroAcessoRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.EnviarEmailRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.EnviarEmailSegurancaResponseDTO;
import com.fabriciosanches.fichatecnica.dtos.TrocarSenhaRequestDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.properties.FichaTecnicaProperty;
import com.fabriciosanches.fichatecnica.security.DadosTokenJWT;
import com.fabriciosanches.fichatecnica.security.UsuarioSecurityDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ficha-tecnica")
@Tag(name = "Segurança", description = "Fluxos de recuperação de senha, envio de e-mail e troca de credenciais")
public class SegurancaController {

    private static final Logger logger = LogManager.getLogger(SegurancaController.class);

    private final RecuperacaoSenhaPort recuperacaoSenhaPort;
    private final AuthenticationManager authenticationManager;
    private final GerarTokenPort gerarTokenPort;
    private final FichaTecnicaProperty fichaTecnicaProperty;

    public SegurancaController(RecuperacaoSenhaPort recuperacaoSenhaPort,
                               AuthenticationManager authenticationManager,
                               GerarTokenPort gerarTokenPort,
                               FichaTecnicaProperty fichaTecnicaProperty) {
        this.recuperacaoSenhaPort = recuperacaoSenhaPort;
        this.authenticationManager = authenticationManager;
        this.gerarTokenPort = gerarTokenPort;
        this.fichaTecnicaProperty = fichaTecnicaProperty;
    }

    @PostMapping("/login-recuperacao-senha")
    @Operation(summary = "Login técnico para recuperação de senha", description = "Emite um JWT técnico de curta duração para os fluxos de recuperação e primeiro acesso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token técnico emitido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "403", description = "Usuário de sistema sem ROLE_SYSTEM"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o token")
    })
    public ResponseEntity<DadosTokenJWT> loginRecuperacaoSenha() {
        String systemUsername = fichaTecnicaProperty.getSystem().getUsername();
        String systemPassword = fichaTecnicaProperty.getSystem().getPassword();

        if (systemUsername == null || systemUsername.isBlank() || systemPassword == null || systemPassword.isBlank()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DadosTokenJWT("Credenciais do usuário de sistema não configuradas"));
        }

        try {
            var userPwd = new UsernamePasswordAuthenticationToken(systemUsername, systemPassword);
            var authentication = authenticationManager.authenticate(userPwd);
            UserDetails principal = (UserDetails) authentication.getPrincipal();

            boolean hasSystemRole = principal.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_SYSTEM".equalsIgnoreCase(a.getAuthority()));
            if (!hasSystemRole) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new DadosTokenJWT("Usuário de sistema não possui ROLE_SYSTEM"));
            }

            Usuario usuario = toUsuario(principal);
            return ResponseEntity.ok(gerarTokenPort.gerarToken(usuario));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new DadosTokenJWT("Credenciais do usuário de sistema inválidas"));
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().body(new DadosTokenJWT(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new DadosTokenJWT(e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('SYSTEM', 'ADMIN')")
    @PostMapping("/enviar-email-seguranca")
    @Transactional
    @Operation(summary = "Envia e-mail de segurança", description = "Dispara um e-mail para validação do fluxo de recuperação de senha.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "E-mail enviado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para envio"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao enviar o e-mail")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EnviarEmailSegurancaResponseDTO> enviarEmailSeguranca(@RequestBody EnviarEmailRequestDTO email) {
        try {
            return ResponseEntity.ok(recuperacaoSenhaPort.enviarEmailSeguranca(email.email()));
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().build();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAnyRole('SYSTEM', 'ADMIN')")
    @PostMapping("/enviar-email-primeiro-acesso")
    @Transactional
    @Operation(summary = "Envia e-mail de primeiro acesso", description = "Envia um e-mail com instruções para o primeiro acesso do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "E-mail enviado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para envio"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao enviar o e-mail")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> enviarEmailPrimeiroAcesso(@RequestBody EnviarEmailPrimeiroAcessoRequestDTO dados) {
        try {
            recuperacaoSenhaPort.enviarEmailPrimeiroAcesso(dados);
            return ResponseEntity.ok().build();
        } catch (FichaTecnicaException e) {
            return ResponseEntity.badRequest().build();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAnyRole('SYSTEM', 'ADMIN')")
    @PostMapping("/trocar-senha")
    @Transactional
    @Operation(summary = "Troca senha", description = "Valida os dados de segurança e altera a senha do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha trocada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para troca de senha"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao trocar a senha")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> trocarSenha(@RequestBody TrocarSenhaRequestDTO trocarDTO) {
        try {
            recuperacaoSenhaPort.trocarSenhaSeguranca(trocarDTO.email(), trocarDTO.cpf(), trocarDTO.tokenSeguranca(),
                    trocarDTO.senha(), trocarDTO.confirmacaoSenha());
            return ResponseEntity.ok().build();
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao trocar senha", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

