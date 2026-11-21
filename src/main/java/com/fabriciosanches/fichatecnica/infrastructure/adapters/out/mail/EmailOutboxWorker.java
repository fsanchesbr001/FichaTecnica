package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

import com.fabriciosanches.fichatecnica.core.ports.out.EntregarEmailPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@ConditionalOnProperty(name = "ficha-tecnica.mail.outbox.worker-enabled", havingValue = "true", matchIfMissing = true)
public class EmailOutboxWorker {

    private static final Logger logger = LogManager.getLogger(EmailOutboxWorker.class);
    private final EmailOutboxStore store;
    private final EmailPayloadCipher cipher;
    private final EntregarEmailPort delivery;

    public EmailOutboxWorker(EmailOutboxStore store, EmailPayloadCipher cipher, EntregarEmailPort delivery) {
        this.store = store;
        this.cipher = cipher;
        this.delivery = delivery;
    }

    @Scheduled(fixedDelayString = "${ficha-tecnica.mail.outbox.poll-delay-ms:5000}")
    public void dispatch() {
        for (int i = 0; i < 20; i++) {
            var claimed = store.claim(now());
            if (claimed.isEmpty()) {
                return;
            }
            var claim = claimed.get();
            if (claim.payload() == null) {
                logger.warn("Email outbox {} expirado; gere novas credenciais para reenvio", claim.id());
                continue;
            }
            try {
                delivery.entregar(cipher.decrypt(claim.payload()));
            } catch (RuntimeException e) {
                // Exceptions from providers may contain credentials or message bodies.
                String error = e instanceof EmailDeliveryException ? e.getMessage() : e.getClass().getSimpleName();
                boolean retried = store.retry(claim, now(), error);
                logger.error("Email outbox {} falhou na tentativa {}: {}; retry registrado={}",
                        claim.id(), claim.attempts(), error, retried);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                continue;
            }
            if (store.sent(claim, now())) {
                logger.info("Email outbox {} entregue na tentativa {}", claim.id(), claim.attempts());
            } else {
                logger.warn("Email outbox {} entregue, mas lease mudou; entrega pode ser duplicada", claim.id());
            }
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
