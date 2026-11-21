package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

public class EmailDeliveryException extends IllegalStateException {

    public EmailDeliveryException(String code) {
        super(code);
    }

    public EmailDeliveryException(String code, Throwable cause) {
        super(code, cause);
    }
}
