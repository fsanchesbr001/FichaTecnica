package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

import com.fabriciosanches.fichatecnica.core.domain.MensagemEmail;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailPayloadCipherTest {

    private final EmailPayloadCipher cipher = new EmailPayloadCipher(new ObjectMapper(),
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");

    @Test
    void protegeCredenciaisComIvAleatorioERecuperaMensagem() {
        var mensagem = new MensagemEmail("test@example.com", "Acesso", "senha-temporaria", true);
        String first = cipher.encrypt(mensagem);
        String second = cipher.encrypt(mensagem);

        assertThat(first).isNotEqualTo(second).doesNotContain("senha-temporaria");
        assertThat(cipher.decrypt(first)).isEqualTo(mensagem);
    }

    @Test
    void rejeitaPayloadAdulterado() {
        byte[] bytes = Base64.getDecoder().decode(cipher.encrypt(
                new MensagemEmail("test@example.com", "Acesso", "senha-temporaria", true)));
        bytes[bytes.length - 1] ^= 1;

        assertThatThrownBy(() -> cipher.decrypt(Base64.getEncoder().encodeToString(bytes)))
                .isInstanceOf(IllegalStateException.class);
    }
}
