package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

import com.fabriciosanches.fichatecnica.core.domain.MensagemEmail;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SmtpEmailAdapterTest {

    @Test
    void falhaSmtpDevePropagarParaRetryDaOutbox() {
        var sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        doThrow(new MailSendException("SMTP indisponivel")).when(sender).send(any(MimeMessage.class));
        var adapter = new SmtpEmailAdapter(sender, "sender@example.com");

        assertThatThrownBy(() -> adapter.entregar(new MensagemEmail("test@example.com", "Acesso", "Texto", false)))
                .isInstanceOf(MailSendException.class);
    }
}
