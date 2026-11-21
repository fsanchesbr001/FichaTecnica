CREATE TABLE email_outbox (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    payload TEXT NULL,
    message_key VARCHAR(64) NULL,
    status VARCHAR(16) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    next_attempt_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NULL,
    lease_until DATETIME(6) NULL,
    lease_token VARCHAR(36) NULL,
    sent_at DATETIME(6) NULL,
    last_error VARCHAR(100) NULL,
    INDEX idx_email_outbox_pending (status, next_attempt_at),
    INDEX idx_email_outbox_lease (status, lease_until),
    INDEX idx_email_outbox_message (message_key, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
