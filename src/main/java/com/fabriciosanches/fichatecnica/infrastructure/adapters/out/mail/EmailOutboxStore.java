package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EmailOutboxStore {

    private final JdbcTemplate jdbc;

    public EmailOutboxStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(String payload, LocalDateTime expiresAt, String messageKey) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (messageKey != null) {
            jdbc.update("""
                    UPDATE email_outbox SET status = 'CANCELLED', payload = NULL,
                        lease_until = NULL, lease_token = NULL, last_error = 'Credenciais substituidas'
                    WHERE message_key = ? AND status IN ('PENDING', 'PROCESSING')
                    """, messageKey);
        }
        jdbc.update("""
                INSERT INTO email_outbox
                    (id, payload, message_key, status, attempts, created_at, next_attempt_at, expires_at)
                VALUES (?, ?, ?, 'PENDING', 0, ?, ?, ?)
                """, UUID.randomUUID().toString(), payload, messageKey, now, now, expiresAt);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Claim> claim(LocalDateTime now) {
        var rows = jdbc.query("""
                SELECT id, payload, attempts, expires_at FROM email_outbox
                WHERE (status = 'PENDING' AND next_attempt_at <= ?)
                   OR (status = 'PROCESSING' AND lease_until <= ?)
                ORDER BY next_attempt_at, id LIMIT 1 FOR UPDATE SKIP LOCKED
                """, (rs, row) -> new Claim(rs.getString("id"), rs.getString("payload"),
                rs.getInt("attempts") + 1, UUID.randomUUID().toString(),
                rs.getObject("expires_at", LocalDateTime.class)), now, now);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Claim claim = rows.get(0);
        if (claim.expiresAt() != null && !claim.expiresAt().isAfter(now)) {
            jdbc.update("""
                    UPDATE email_outbox SET status = 'EXPIRED', payload = NULL,
                        lease_until = NULL, lease_token = NULL, last_error = 'Credenciais expiradas'
                    WHERE id = ?
                    """, claim.id());
            return Optional.of(new Claim(claim.id(), null, claim.attempts(), claim.leaseToken(), claim.expiresAt()));
        }
        jdbc.update("""
                UPDATE email_outbox SET status = 'PROCESSING', attempts = ?,
                    lease_until = ?, lease_token = ? WHERE id = ?
                """, claim.attempts(), now.plusMinutes(2), claim.leaseToken(), claim.id());
        return Optional.of(claim);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean sent(Claim claim, LocalDateTime now) {
        return jdbc.update("""
                UPDATE email_outbox SET status = 'SENT', sent_at = ?, payload = NULL,
                    lease_until = NULL, lease_token = NULL, last_error = NULL
                WHERE id = ? AND status = 'PROCESSING' AND lease_token = ?
                """, now, claim.id(), claim.leaseToken()) == 1;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean retry(Claim claim, LocalDateTime now, String error) {
        long delay = Math.min(3600, 30L << Math.min(claim.attempts() - 1, 7));
        return jdbc.update("""
                UPDATE email_outbox SET status = 'PENDING', next_attempt_at = ?,
                    lease_until = NULL, lease_token = NULL, last_error = ?
                WHERE id = ? AND status = 'PROCESSING' AND lease_token = ?
                """, now.plusSeconds(delay), error, claim.id(), claim.leaseToken()) == 1;
    }

    public record Claim(String id, String payload, int attempts, String leaseToken, LocalDateTime expiresAt) {
    }
}
