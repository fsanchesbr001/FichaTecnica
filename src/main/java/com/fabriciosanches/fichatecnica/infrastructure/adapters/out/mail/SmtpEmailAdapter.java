package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

import com.fabriciosanches.fichatecnica.core.domain.MensagemEmail;
import com.fabriciosanches.fichatecnica.core.ports.out.EntregarEmailPort;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ficha-tecnica.mail.provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpEmailAdapter implements EntregarEmailPort {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailAdapter(JavaMailSender mailSender, @Value("${ficha-tecnica.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void entregar(MensagemEmail mensagem) {
        try {
            var mimeMessage = mailSender.createMimeMessage();
            var email = new MimeMessageHelper(mimeMessage, mensagem.html(), "UTF-8");
            email.setTo(mensagem.destinatario());
            email.setSubject(mensagem.assunto());
            email.setFrom(from);
            email.setText(mensagem.conteudo(), mensagem.html());
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Falha ao preparar email SMTP", e);
        }
    }
}
