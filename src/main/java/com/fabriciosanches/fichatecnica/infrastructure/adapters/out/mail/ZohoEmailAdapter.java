package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

import com.fabriciosanches.fichatecnica.core.domain.MensagemEmail;
import com.fabriciosanches.fichatecnica.core.ports.out.EntregarEmailPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "ficha-tecnica.mail.provider", havingValue = "zoho")
public class ZohoEmailAdapter implements EntregarEmailPort {

    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String from;
    private final URI tokenUri;
    private final URI mailUri;

    public ZohoEmailAdapter(ObjectMapper mapper,
                            @Value("${ficha-tecnica.mail.zoho.client-id:}") String clientId,
                            @Value("${ficha-tecnica.mail.zoho.client-secret:}") String clientSecret,
                            @Value("${ficha-tecnica.mail.zoho.refresh-token:}") String refreshToken,
                            @Value("${ficha-tecnica.mail.zoho.account-id:}") String accountId,
                            @Value("${ficha-tecnica.mail.from}") String from,
                            @Value("${ficha-tecnica.mail.zoho.accounts-url:https://accounts.zoho.com}") URI accountsUrl,
                            @Value("${ficha-tecnica.mail.zoho.mail-url:https://mail.zoho.com}") URI mailUrl) {
        if (clientId.isBlank() || clientSecret.isBlank() || refreshToken.isBlank()
                || accountId.isBlank() || from.isBlank()) {
            throw new IllegalArgumentException("Configure ZOHO_CLIENT_ID, ZOHO_CLIENT_SECRET, "
                    + "ZOHO_REFRESH_TOKEN, ZOHO_ACCOUNT_ID e MAIL_FROM");
        }
        this.mapper = mapper;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.from = from;
        tokenUri = accountsUrl.resolve("/oauth/v2/token");
        mailUri = mailUrl.resolve("/api/accounts/" + URLEncoder.encode(accountId, StandardCharsets.UTF_8) + "/messages");
    }

    @Override
    public void entregar(MensagemEmail mensagem) {
        Map<String, String> credentials = Map.of("client_id", clientId, "client_secret", clientSecret,
                "refresh_token", refreshToken, "grant_type", "refresh_token");
        String form = credentials.entrySet().stream().map(entry ->
                encode(entry.getKey()) + "=" + encode(entry.getValue())).collect(Collectors.joining("&"));
        JsonNode token = post(tokenUri, "application/x-www-form-urlencoded", form, null);
        JsonNode accessToken = token.get("access_token");
        if (accessToken == null || !accessToken.isTextual() || accessToken.asText().isBlank()) {
            throw new EmailDeliveryException("ZOHO_OAUTH_TOKEN_MISSING");
        }
        try {
            String body = mapper.writeValueAsString(Map.of(
                    "fromAddress", from, "toAddress", mensagem.destinatario(),
                    "subject", mensagem.assunto(), "content", mensagem.conteudo(),
                    "mailFormat", mensagem.html() ? "html" : "plaintext"));
            JsonNode response = post(mailUri, "application/json", body, accessToken.asText());
            if (response.path("status").path("code").asInt() != 200
                    || response.path("data").path("messageId").asText().isBlank()) {
                throw new EmailDeliveryException("ZOHO_MAIL_NOT_ACCEPTED_"
                        + response.path("status").path("code").asInt());
            }
        } catch (IOException e) {
            throw new EmailDeliveryException("ZOHO_PAYLOAD_INVALID", e);
        }
    }

    private JsonNode post(URI uri, String contentType, String body, String accessToken) {
        var request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (accessToken != null) {
            request.header("Authorization", "Zoho-oauthtoken " + accessToken);
        }
        try {
            var response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new EmailDeliveryException("ZOHO_HTTP_" + response.statusCode());
            }
            JsonNode json = mapper.readTree(response.body());
            if (json == null || !json.isObject()) {
                throw new EmailDeliveryException("ZOHO_RESPONSE_INVALID");
            }
            return json;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmailDeliveryException("ZOHO_INTERRUPTED", e);
        } catch (IOException e) {
            throw new EmailDeliveryException("ZOHO_IO_FAILURE", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
