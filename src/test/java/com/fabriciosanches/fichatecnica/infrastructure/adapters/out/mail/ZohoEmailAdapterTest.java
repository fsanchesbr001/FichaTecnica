package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

import com.fabriciosanches.fichatecnica.core.domain.MensagemEmail;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZohoEmailAdapterTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicReference<String> tokenBody = new AtomicReference<>();
    private final AtomicReference<String> mailBody = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicInteger mailCalls = new AtomicInteger();
    private HttpServer server;
    private ZohoEmailAdapter adapter;
    private int tokenStatus = 200;
    private String tokenResponse = "{\"access_token\":\"test-token\"}";
    private int mailStatus = 200;
    private String mailResponse = "{\"status\":{\"code\":200},\"data\":{\"messageId\":\"message-1\"}}";

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/oauth/v2/token", exchange -> {
            tokenBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = tokenResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(tokenStatus, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.createContext("/api/accounts/test-account/messages", exchange -> {
            mailCalls.incrementAndGet();
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            mailBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = mailResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(mailStatus, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        URI base = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
        adapter = new ZohoEmailAdapter(mapper, "test-client", "secret+&=", "test-refresh",
                "test-account", "sender@example.com", base, base);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void enviaHtmlViaOAuthEApiSemSmtp() throws IOException {
        adapter.entregar(new MensagemEmail("recipient@example.com", "Primeiro acesso", "<p>Senha</p>", true));

        assertThat(tokenBody.get()).contains("grant_type=refresh_token", "client_id=test-client",
                "refresh_token=test-refresh", "client_secret=secret%2B%26%3D");
        assertThat(authorization.get()).isEqualTo("Zoho-oauthtoken test-token");
        var body = mapper.readTree(mailBody.get());
        assertThat(body.path("fromAddress").asText()).isEqualTo("sender@example.com");
        assertThat(body.path("toAddress").asText()).isEqualTo("recipient@example.com");
        assertThat(body.path("mailFormat").asText()).isEqualTo("html");
        assertThat(body.path("content").asText()).isEqualTo("<p>Senha</p>");
    }

    @Test
    void oauthInvalidoNaoEnviaMensagemNemExpoeRespostaSensivel() {
        tokenResponse = "{\"error\":\"test-refresh secret+&=\"}";

        assertThatThrownBy(() -> adapter.entregar(new MensagemEmail("recipient@example.com", "Teste", "Texto", false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("ZOHO_OAUTH_TOKEN_MISSING");
        assertThat(mailCalls.get()).isZero();
    }

    @ParameterizedTest
    @CsvSource({
            "500, '{\"status\":{\"code\":500}}'",
            "200, '{\"status\":{\"code\":400}}'",
            "200, '{\"status\":{\"code\":200}}'"
    })
    void falhasHttpOuDaApiNaoSaoTratadasComoSucesso(int status, String response) {
        mailStatus = status;
        mailResponse = response;

        assertThatThrownBy(() -> adapter.entregar(new MensagemEmail("recipient@example.com", "Teste", "Texto", false)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mailCalls.get()).isEqualTo(1);
    }

    @Test
    void configuraCredenciaisObrigatoriasAntesDeIniciar() {
        assertThatThrownBy(() -> new ZohoEmailAdapter(mapper, "", "", "", "", "sender@example.com",
                URI.create("https://accounts.zoho.com"), URI.create("https://mail.zoho.com")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
