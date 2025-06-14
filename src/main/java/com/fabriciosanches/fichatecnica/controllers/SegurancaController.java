package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.constants.Constants;
import com.fabriciosanches.fichatecnica.domains.Seguranca;
import com.fabriciosanches.fichatecnica.dtos.*;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.SegurancaService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ficha-tecnica")
public class SegurancaController {

    private static final Logger logger = LogManager.getLogger(SegurancaController.class);

    final SegurancaService segurancaService;

    public SegurancaController(SegurancaService segurancaService) {
        this.segurancaService = segurancaService;
    }

    //Testado
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

    //Testado
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

    //Testado
    @PostMapping("/bloqueio-administrativo")
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

    //Testado
    @PostMapping("/desbloqueio-administrativo")
    @Transactional
    public ResponseEntity<BloqueiosResponseDTO> desbloqueioAdministrativo(@RequestBody BloqueiosRequestDTO bloqueiosRequestDTO) {
        logger.info("Inicio do método desbloqueioAdministrativo");
        logger.info("Parâmetros de entrada: {}", bloqueiosRequestDTO);
        try {
            BloqueiosResponseDTO bloqueiosResponseDTO =  segurancaService.desbloqueioAdmSeguranca(bloqueiosRequestDTO);
            logger.info("Desbloqueio administrativo realizado com sucesso");
            logger.info("Fim do método desbloqueioAdministrativo");
            return ResponseEntity.ok(bloqueiosResponseDTO);
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao realizar Desbloqueio Administrativo", e);
            return ResponseEntity.badRequest().body(new BloqueiosResponseDTO(Constants.MSG_ERRO_BLOQUEIO));
        }
    }

    //Testado
    @PostMapping("/registrar-usuario")
    @Transactional
    public ResponseEntity<?> registrarUsuario(@RequestBody RegisterDTO dados) {
        logger.info("Inicio do método registrarUsuario");
        logger.info("Parâmetros de entrada: {}", dados);
        try {
            segurancaService.registrarUsuario(dados);
            logger.info("Usuário registrado com sucesso");
            logger.info("Fim do método registrarUsuario");
            return ResponseEntity.ok().build();
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao registrar usuário", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Testado
    @PostMapping("/excluir-usuario")
    @Transactional
    public ResponseEntity<?> excluirUsuario(@RequestBody BloqueiosRequestDTO dados) {
        logger.info("Inicio do método excluirUsuario");
        logger.info("Parâmetros de entrada: {}", dados);
        try {
            segurancaService.excluirUsuario(dados.email());
            logger.info("Usuário excluído com sucesso");
            logger.info("Fim do método excluirUsuario");
            return ResponseEntity.ok().build();
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao excluir usuário", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    //Testado
    @PostMapping("/resetar-senha")
    @Transactional
    public ResponseEntity<?> resetarSenhaUsuario(@RequestBody BloqueiosRequestDTO dados) {
        logger.info("Inicio do método resetarSenhaUsuario");
        logger.info("Parâmetros de entrada: {}", dados);
        try {
            segurancaService.expirarSenha(dados.email());
            logger.info("Senha do usuário expirada com sucesso");
            logger.info("Fim do método resetarSenhaUsuario");
            return ResponseEntity.ok().build();
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao resetar senha do usuário", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/listar-todos-usuarios")

    public ResponseEntity<List<SegurancaDTO>> listarTodosUsuarios() {
        logger.info("Inicio do método listarTodosUsuarios");
        try {
            List<Seguranca> response = segurancaService.findAll();
            logger.info("Lista de usuários obtida com sucesso");
            if(response.isEmpty()) {
                logger.info("Nenhum usuário encontrado");
                return ResponseEntity.noContent().build();
            }
            List<SegurancaDTO> segurancaDTOList = response.stream()
                    .map(SegurancaDTO::new)
                    .toList();
            logger.info("Fim do método listarTodosUsuarios");
            return ResponseEntity.ok(segurancaDTOList);
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao listar usuários", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/buscar-usuario/{email}")
    public ResponseEntity<SegurancaDTO> buscarUsuarioPorEmail(@PathVariable String email) {
        logger.info("Inicio do método buscarUsuarioPorEmail");
        logger.info("Parâmetro de entrada: {}", email);
        try {
            Seguranca seguranca = segurancaService.findByEmail(email);
            if (seguranca == null) {
                logger.warn("Usuário não encontrado para o email: {}", email);
                return ResponseEntity.notFound().build();
            }
            SegurancaDTO segurancaDTO = new SegurancaDTO(seguranca);
            logger.info("Usuário encontrado com sucesso");
            logger.info("Fim do método buscarUsuarioPorEmail");
            return ResponseEntity.ok(segurancaDTO);
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao buscar usuário por email", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
