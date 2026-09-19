package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

import com.fabriciosanches.fichatecnica.core.ports.out.EnviarEmailPort;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;
import java.util.Objects;

@Component
public class SmtpEmailAdapter implements EnviarEmailPort {

    private static final Logger logger = LogManager.getLogger(SmtpEmailAdapter.class);

    private final Environment environment;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public SmtpEmailAdapter(JavaMailSender mailSender, Environment environment, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.environment = environment;
        this.templateEngine = templateEngine;
    }

    @Override
    public void enviar(String destinatario, String assunto, String corpoTexto) {
        try {
            MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            MimeMessageHelper email = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            email.setTo(destinatario);
            email.setSubject(assunto);
            email.setFrom(Objects.requireNonNull(environment.getProperty("ficha-tecnica.mail.from")));
            email.setText(corpoTexto, false);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            logger.error("Erro ao enviar email para {}", destinatario, e);
        }
    }

    @Override
    public void enviarComTemplate(String destinatario, String assunto, String nomeTemplate, Map<String, Object> variaveis) {
        try {
            Context context = new Context();
            if (variaveis != null) {
                variaveis.forEach(context::setVariable);
            }
            String corpoHtml = templateEngine.process(nomeTemplate, context);
            
            MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            MimeMessageHelper email = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            email.setTo(destinatario);
            email.setSubject(assunto);
            email.setFrom(Objects.requireNonNull(environment.getProperty("ficha-tecnica.mail.from")));
            email.setText(corpoHtml, true);
            mailSender.send(mimeMessage);
            logger.info("Email enviado com sucesso para {} usando template {}", destinatario, nomeTemplate);
        } catch (Exception e) {
            logger.error("Erro ao enviar email com template para {}", destinatario, e);
        }
    }
}


