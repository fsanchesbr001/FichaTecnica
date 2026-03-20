package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.domains.Usuario;
import com.fabriciosanches.fichatecnica.dtos.*;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.properties.FichaTecnicaProperty;
import com.fabriciosanches.fichatecnica.security.DadosTokenJWT;
import com.fabriciosanches.fichatecnica.security.TokenService;
import com.fabriciosanches.fichatecnica.services.SegurancaService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("ficha-tecnica")
public class SegurancaController {

    private static final Logger logger = LogManager.getLogger(SegurancaController.class);

    private final SegurancaService segurancaService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final FichaTecnicaProperty fichaTecnicaProperty;

    public SegurancaController(SegurancaService segurancaService,
                               AuthenticationManager authenticationManager,
                               TokenService tokenService,
                               FichaTecnicaProperty fichaTecnicaProperty) {
        this.segurancaService = segurancaService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.fichaTecnicaProperty = fichaTecnicaProperty;
    }

    /**
     * Endpoint público para o fluxo de esqueci a senha / primeiro acesso.
     * <p>
     * O frontend chama este endpoint sem autenticação prévia. Internamente,
     * o sistema autentica com o usuário técnico configurado nas variáveis de
     * ambiente {@code SYSTEM_USER} / {@code SYSTEM_PWD} e retorna um JWT com
     * perfil ROLE_SYSTEM, que será usado exclusivamente nos passos seguintes
     * do fluxo de recuperação de senha (envio de e-mail, troca de senha).
     * </p>
     *
     * @return {@link DadosTokenJWT} com o token técnico de curta duração
     */
    @PostMapping("/login-recuperacao-senha")
    public ResponseEntity<DadosTokenJWT> loginRecuperacaoSenha() {
        logger.info("Inicio do método loginRecuperacaoSenha");

        String systemUsername = fichaTecnicaProperty.getSystem().getUsername();
        String systemPassword = fichaTecnicaProperty.getSystem().getPassword();

        if (systemUsername == null || systemUsername.isBlank()
                || systemPassword == null || systemPassword.isBlank()) {
            logger.error("Credenciais do usuário de sistema não configuradas (SYSTEM_USER / SYSTEM_PWD)");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DadosTokenJWT("Credenciais do usuário de sistema não configuradas"));
        }

        try {
            var userPwd = new UsernamePasswordAuthenticationToken(systemUsername, systemPassword);
            var authentication = authenticationManager.authenticate(userPwd);

            var usuario = (Usuario) authentication.getPrincipal();

            if (usuario.getRole() == null
                    || !"ROLE_SYSTEM".equalsIgnoreCase(usuario.getRole().getRole())) {
                logger.warn("Usuário de sistema '{}' não possui ROLE_SYSTEM - acesso negado", systemUsername);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new DadosTokenJWT("Usuário de sistema não possui ROLE_SYSTEM"));
            }

            var token          = tokenService.gerarToken(usuario);
            var expMinutes     = tokenService.getExpirationMinutes();
            var expiresAt      = tokenService.getTokenExpiresAt();
            var role           = usuario.getRole().getRole();

            logger.info("Token técnico emitido com sucesso para recuperação de senha (usuário: {})", systemUsername);
            return ResponseEntity.ok(new DadosTokenJWT(
                    token, expMinutes, expiresAt,
                    usuario.getLogin(), usuario.getNome(), role
            ));

        } catch (BadCredentialsException e) {
            logger.error("Credenciais do usuário de sistema inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new DadosTokenJWT("Credenciais do usuário de sistema inválidas"));
        } catch (FichaTecnicaException e) {
            logger.error("Erro de regra de negócio no login técnico: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new DadosTokenJWT(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado no login técnico", e);
            return ResponseEntity.internalServerError().body(new DadosTokenJWT(e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Segurança: endpoints relacionados a envio de email de segurança e troca de senha
    // ─────────────────────────────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('SYSTEM', 'ADMIN')")
    @PostMapping("/enviar-email-seguranca")
    @Transactional
    public ResponseEntity<EnviarEmailSegurancaResponseDTO> enviarEmailSeguranca(@RequestBody EnviarEmailRequestDTO email) {
        logger.info("Inicio do método enviarEmail");
        logger.info("Parâmetros de entrada: {}", email);
        try {
            EnviarEmailSegurancaResponseDTO enviarEmailResponseDTO = segurancaService.enviarEmailSeguranca(email.email());
            logger.info("Email de segurança enviado com sucesso");
            logger.info("Fim do método enviarEmail");
            return ResponseEntity.ok(enviarEmailResponseDTO);
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao enviar Email de segurança", e);
            return ResponseEntity.badRequest().build();
        } catch (MessagingException e) {
            logger.error("Erro de Messaging ", e);
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAnyRole('SYSTEM', 'ADMIN')")
    @PostMapping("/enviar-email-primeiro-acesso")
    @Transactional
    public ResponseEntity<Void> enviarEmailPrimeiroAcesso(@RequestBody EnviarEmailPrimeiroAcessoRequestDTO dados) {
        logger.info("Inicio do método enviarEmailPrimeiroAcesso");
        logger.info("Parâmetros de entrada: {}", dados);
        try {
            segurancaService.enviarEmailPrimeiroAcesso(dados);
            logger.info("Email de primeiro acesso enviado com sucesso");
            logger.info("Fim do método enviarEmailPrimeiroAcesso");
            return ResponseEntity.ok().build();
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao enviar Email de primeiro acesso", e);
            return ResponseEntity.badRequest().build();
        } catch (MessagingException e) {
            logger.error("Erro de Messaging ", e);
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAnyRole('SYSTEM', 'ADMIN')")
    @PostMapping("/trocar-senha")
    @Transactional
    public ResponseEntity<?> trocarSenha(@RequestBody TrocarSenhaRequestDTO trocarDTO) {
        logger.info("Inicio do método trocarSenha");
        logger.info("Parâmetros de entrada: {}", trocarDTO);
        try {
            segurancaService.trocarSenhaSeguranca(trocarDTO.email(), trocarDTO.cpf(), trocarDTO.tokenSeguranca(),
                    trocarDTO.senha(), trocarDTO.confirmacaoSenha());
            logger.info("Senha trocada com sucesso");
            logger.info("Fim do método trocarSenha");
            return ResponseEntity.ok().build();
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao trocar senha", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
