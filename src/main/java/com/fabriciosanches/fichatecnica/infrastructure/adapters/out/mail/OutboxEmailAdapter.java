package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

import com.fabriciosanches.fichatecnica.core.domain.MensagemEmail;
import com.fabriciosanches.fichatecnica.core.ports.out.EnviarEmailPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;

@Component
@Transactional(propagation = Propagation.MANDATORY)
public class OutboxEmailAdapter implements EnviarEmailPort {

    private final EmailOutboxStore store;
    private final EmailPayloadCipher cipher;
    private final SpringTemplateEngine templateEngine;

    public OutboxEmailAdapter(EmailOutboxStore store, EmailPayloadCipher cipher, SpringTemplateEngine templateEngine) {
        this.store = store;
        this.cipher = cipher;
        this.templateEngine = templateEngine;
    }

    @Override
    public void enviar(String destinatario, String assunto, String corpoTexto) {
        store.enqueue(cipher.encrypt(new MensagemEmail(destinatario, assunto, corpoTexto, false)), null, null);
    }

    @Override
    public void enviarComTemplate(String destinatario, String assunto, String nomeTemplate,
                                 Map<String, Object> variaveis) {
        enviarComTemplate(destinatario, assunto, nomeTemplate, variaveis, null);
    }

    @Override
    public void enviarComTemplate(String destinatario, String assunto, String nomeTemplate,
                                 Map<String, Object> variaveis, LocalDateTime expiraEm) {
        Context context = new Context();
        if (variaveis != null) {
            variaveis.forEach(context::setVariable);
        }
        String html = templateEngine.process(nomeTemplate, context);
        LocalDateTime expiresAt = expiraEm == null ? null :
                expiraEm.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        String messageKey = expiresAt == null ? null : messageKey(destinatario, nomeTemplate);
        store.enqueue(cipher.encrypt(new MensagemEmail(destinatario, assunto, html, true)), expiresAt, messageKey);
    }

    private String messageKey(String destinatario, String template) {
        try {
            byte[] identity = (destinatario.toLowerCase(Locale.ROOT) + "\n" + template)
                    .getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(identity));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }
}
