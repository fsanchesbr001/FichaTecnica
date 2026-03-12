package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.*;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.SegurancaService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("ficha-tecnica")
public class SegurancaController {

    private static final Logger logger = LogManager.getLogger(SegurancaController.class);

    final SegurancaService segurancaService;

    public SegurancaController(SegurancaService segurancaService) {
        this.segurancaService = segurancaService;
    }

    // Segurança: endpoints relacionados a envio de email de segurança e troca de senha
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
        }
        catch (FichaTecnicaException e){
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
        }
        catch (FichaTecnicaException e){
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
