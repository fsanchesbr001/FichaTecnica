package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.constants.Constants;
import com.fabriciosanches.fichatecnica.dtos.*;
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
    public ResponseEntity<EnviarEmailResponseDTO> enviarEmail(@RequestBody EnviarEmailRequestDTO email) {
        logger.info("Inicio do método enviarEmail");
        logger.info("Parâmetros de entrada: {}", email);
        try {
            EnviarEmailResponseDTO enviarEmailResponseDTO = segurancaService.enviarEmailSeguranca(email.email());
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

    @PostMapping("/bloq-primeiro-acesso")
    @Transactional
    public ResponseEntity<BloqueiosResponseDTO> bloqueioPrimeiroAcesso(@RequestBody BloqueiosRequestDTO bloqueiosRequestDTO) {
        logger.info("Inicio do método bloqueioPrimeiroAcesso");
        logger.info("Parâmetros de entrada: {}", bloqueiosRequestDTO);
        try {
            BloqueiosResponseDTO bloqueiosResponseDTO =  segurancaService.primeiroAcessoSeguranca(bloqueiosRequestDTO);
            logger.info("Primeiro acesso realizado com sucesso");
            logger.info("Fim do método primeiroAcesso");
            return ResponseEntity.ok(bloqueiosResponseDTO);
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao realizar primeiro acesso", e);
            return ResponseEntity.badRequest().body(new BloqueiosResponseDTO(Constants.MSG_ERRO_BLOQUEIO));
        }
    }

    @PostMapping("/bloq-administrativo")
    @Transactional
    public ResponseEntity<BloqueiosResponseDTO> bloqueioAdministrativo(@RequestBody BloqueiosRequestDTO bloqueiosRequestDTO) {
        logger.info("Inicio do método bloqueioAdministrativo");
        logger.info("Parâmetros de entrada: {}", bloqueiosRequestDTO);
        try {
            BloqueiosResponseDTO bloqueiosResponseDTO =  segurancaService.bloqueioAdmSeguranca(bloqueiosRequestDTO);
            logger.info("Bloqueio administrativo realizado com sucesso");
            logger.info("Fim do método bloqueioAdministrativo");
            return ResponseEntity.ok(bloqueiosResponseDTO);
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao realizar Bloqueio Administrativo", e);
            return ResponseEntity.badRequest().body(new BloqueiosResponseDTO(Constants.MSG_ERRO_BLOQUEIO));
        }
    }
}
