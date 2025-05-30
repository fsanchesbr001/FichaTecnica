package com.fabriciosanches.fichatecnica.mail;

import com.fabriciosanches.fichatecnica.dtos.EnviarEmailResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Objects;

@Service
public class EmailService {

    private static final Logger logger = LogManager.getLogger(EmailService.class);

    private static final String TEMPLATE_EMAIL="trocasenha";

    private static final String EMAIL_SUBJECT="Recuperação de senha - Ficha Técnica - Ollivander Café";

    private final Environment environment;

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, Environment environment,
                        TemplateEngine templateEngine){
        this.mailSender = mailSender;
        this.environment = environment;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(EnviarEmailResponse dadosContato) throws MessagingException {
        logger.info("Inicio do método sendEmail");
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper email = new MimeMessageHelper(mimeMessage,true,"UTF-8");

        email.setTo(dadosContato.email());
        email.setSubject(EMAIL_SUBJECT);
        email.setFrom(Objects.requireNonNull(environment.getProperty("ficha-tecnica.mail.from")));

        final Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariable("token",dadosContato.tokenSeguranca());
        ctx.setVariable("validadeToken",dadosContato.dataExpiracaoToken());
        final String htmlContent = this.templateEngine.process(TEMPLATE_EMAIL,ctx);
        email.setText(htmlContent,true);
        try {
            logger.info("Enviando email");
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            logger.error("Erro ao enviar email", e);
        }
        finally {
            logger.info("Fim do método sendEmail");
        }
    }
}
