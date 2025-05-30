package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.EnviarEmailResponse;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.SegurancaService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ficha-tecnica")
public class SegurancaController {

    private static final Logger logger = LogManager.getLogger(SegurancaController.class);

    final SegurancaService segurancaService;

    public SegurancaController(SegurancaService segurancaService) {
        this.segurancaService = segurancaService;
    }

    @PostMapping("/enviar-email")
    @Transactional
    public ResponseEntity<EnviarEmailResponse> enviarEmail(@RequestBody String email) {
        logger.info("Inicio do método enviarEmail");
        logger.info("Parâmetros de entrada: {}", email);
        try {
            EnviarEmailResponse enviarEmailResponse = segurancaService.enviarEmailSeguranca(email);
            logger.info("Email de segurança enviado com sucesso");
            logger.info("Fim do método enviarEmail");
            return ResponseEntity.ok(enviarEmailResponse);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao enviar Email de segurança", e);
            return ResponseEntity.badRequest().build();
        } catch (MessagingException e) {
            logger.error("Erro de Messaging ", e);
            throw new RuntimeException(e);
        }
    }
}
