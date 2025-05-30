package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.Seguranca;
import com.fabriciosanches.fichatecnica.dtos.EnviarEmailResponse;
import com.fabriciosanches.fichatecnica.dtos.SegurancaDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.mail.EmailService;
import com.fabriciosanches.fichatecnica.repository.SegurancaRepository;
import com.fabriciosanches.fichatecnica.util.Utilidades;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class SegurancaService {

    private final Logger logger = LogManager.getLogger(SegurancaService.class);
    private final SegurancaRepository repository;
    private final EmailService emailService;

    public SegurancaService(SegurancaRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    private String findCPFByEmail(String email) {
        logger.info("Buscando CPF por email: {}", email);
        String cpf = repository.findCPFByEmail(email);
        if (cpf == null) {
            logger.info("CPF não encontrado para o email: {}", email);
            return null;
        }
        logger.info("CPF encontrado: {}", cpf);
        return cpf;
    }

    public EnviarEmailResponse enviarEmailSeguranca(String email) throws MessagingException {
        logger.info("Preparando email de segurança para: {}", email);
        String cpf = findCPFByEmail(email);
        if (cpf == null) {
            throw new FichaTecnicaException("Email não encontrado");
        }

        if(!Utilidades.validarCPF(cpf)){
            throw new FichaTecnicaException("CPF inválido");
        }

        String token = gerarTokenSeguranca();

        Seguranca seguranca = repository.findByEmail(email);
        seguranca.setTokenSeguranca(token);
        seguranca.setDataExpiracaoToken(LocalDateTime.now().plusMinutes(15));
        logger.info("Token de segurança gerado: {}", token);
        repository.save(seguranca);

        SegurancaDTO segurancaDTO = new SegurancaDTO(seguranca);
        logger.info("Dados de segurança preparados: {}", segurancaDTO);

        EnviarEmailResponse enviarEmailResponse = new EnviarEmailResponse(segurancaDTO);

        logger.info("Enviando email de segurança ");
        emailService.sendEmail(enviarEmailResponse);
        logger.info("Email enviado com sucesso");

        return  enviarEmailResponse;
    }

    private  String gerarTokenSeguranca() {
        Random random = new Random();
        StringBuilder token = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int numero = random.nextInt(10); // Gera um número entre 0 e 9
            token.append(numero);
        }

        return token.toString();

    }

}
