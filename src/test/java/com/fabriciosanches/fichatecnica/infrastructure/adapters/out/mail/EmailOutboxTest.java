package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

import com.fabriciosanches.fichatecnica.core.domain.enums.UserRole;
import com.fabriciosanches.fichatecnica.core.ports.out.EntregarEmailPort;
import com.fabriciosanches.fichatecnica.core.ports.out.SegurancaRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UsuarioRepositoryPort;
import com.fabriciosanches.fichatecnica.core.usecase.SegurancaUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.UsuarioUseCase;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RegisterDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.SegurancaPersistenceAdapter;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UsuarioPersistenceAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.junit.jupiter.api.Assertions.assertTimeout;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.check_nullability=false",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false",
        "ficha-tecnica.mail.outbox.encryption-key=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
@Import({EmailOutboxStore.class, OutboxEmailAdapter.class, EmailPayloadCipher.class,
        UsuarioPersistenceAdapter.class, SegurancaPersistenceAdapter.class, EmailOutboxTest.Config.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class EmailOutboxTest {

    @Autowired private EmailOutboxStore store;
    @Autowired private OutboxEmailAdapter adapter;
    @Autowired private EmailPayloadCipher cipher;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private DataSource dataSource;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private UsuarioRepositoryPort usuarios;
    @Autowired private SegurancaRepositoryPort seguranca;

    private TransactionTemplate transaction;
    private UsuarioUseCase useCase;
    private EntregarEmailPort delivery;

    @BeforeEach
    void setUp() {
        jdbc.execute("SET MODE MySQL");
        jdbc.execute("DROP TABLE IF EXISTS email_outbox");
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V003__Criar-email-outbox.sql"))
                .execute(dataSource);
        jdbc.execute("DROP INDEX IF EXISTS idx_usuarios_login");
        jdbc.execute("DROP INDEX IF EXISTS idx_seguranca_email");
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V004__Indexar-login-email.sql"))
                .execute(dataSource);
        // Match the existing Flyway schema, where these credential fields are nullable.
        jdbc.execute("ALTER TABLE seguranca ALTER COLUMN token_seguranca DROP NOT NULL");
        jdbc.execute("ALTER TABLE seguranca ALTER COLUMN data_expiracao_token DROP NOT NULL");
        jdbc.update("DELETE FROM seguranca");
        jdbc.update("DELETE FROM usuarios");
        transaction = new TransactionTemplate(transactionManager);
        useCase = new UsuarioUseCase(usuarios, seguranca, new SegurancaUseCase(seguranca, usuarios, adapter));
        delivery = mock(EntregarEmailPort.class);
    }

    @Test
    void cadastroPersisteDadosEEmailSemChamarProvedor() {
        register();

        assertThat(count("usuarios")).isEqualTo(1);
        assertThat(count("seguranca")).isEqualTo(1);
        assertThat(count("email_outbox")).isEqualTo(1);
        assertThat(status()).isEqualTo("PENDING");
        String payload = jdbc.queryForObject("SELECT payload FROM email_outbox", String.class);
        assertThat(payload).doesNotContain("new@example.com");
        assertThat(cipher.decrypt(payload).destinatario()).isEqualTo("new@example.com");
        assertThat(cipher.decrypt(payload).html()).isTrue();
        verifyNoInteractions(delivery);
    }

    @Test
    void rollbackAposEnfileirarDesfazCadastroEOutbox() {
        assertThatThrownBy(() -> transaction.executeWithoutResult(tx -> {
            registrarUsuario();
            throw new IllegalStateException("Falha apos enqueue");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(count("usuarios")).isZero();
        assertThat(count("seguranca")).isZero();
        assertThat(count("email_outbox")).isZero();
        verifyNoInteractions(delivery);
    }

    @Test
    void falhaNaOutboxDesfazGravacoesJpa() {
        jdbc.execute("DROP TABLE email_outbox");

        assertThatThrownBy(this::register).isInstanceOf(DataAccessException.class);

        assertThat(count("usuarios")).isZero();
        assertThat(count("seguranca")).isZero();
    }

    @Test
    void enqueueExigeTransacaoDoChamador() {
        assertThatThrownBy(() -> adapter.enviar("new@example.com", "Teste", "Conteudo"))
                .isInstanceOf(IllegalTransactionStateException.class);
        assertThat(count("email_outbox")).isZero();
    }

    @Test
    void falhaAgendaRetryEOutroWorkerEntregaAposReinicio() {
        register();
        doThrow(new IllegalStateException("Provedor indisponivel")).doNothing().when(delivery).entregar(any());
        new EmailOutboxWorker(store, cipher, delivery).dispatch();

        assertThat(status()).isEqualTo("PENDING");
        assertThat(jdbc.queryForObject("SELECT attempts FROM email_outbox", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT next_attempt_at FROM email_outbox", LocalDateTime.class))
                .isAfter(LocalDateTime.now(ZoneOffset.UTC));
        new EmailOutboxWorker(store, cipher, delivery).dispatch();
        verify(delivery, times(1)).entregar(any());

        jdbc.update("UPDATE email_outbox SET next_attempt_at = ?", LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1));
        new EmailOutboxWorker(store, cipher, delivery).dispatch();

        verify(delivery, times(2)).entregar(any());
        assertThat(status()).isEqualTo("SENT");
        assertThat(jdbc.queryForObject("SELECT payload FROM email_outbox", String.class)).isNull();
        assertThat(jdbc.queryForObject("SELECT attempts FROM email_outbox", Integer.class)).isEqualTo(2);
    }

    @Test
    void leaseEvitaClaimsSimultaneosERecuperaWorkerInterrompido() {
        register();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        var first = store.claim(now).orElseThrow();
        assertThat(store.claim(now)).isEmpty();

        var recovered = store.claim(now.plusMinutes(3)).orElseThrow();
        assertThat(recovered.id()).isEqualTo(first.id());
        assertThat(recovered.leaseToken()).isNotEqualTo(first.leaseToken());
        assertThat(store.sent(first, now)).isFalse();
        assertThat(store.retry(first, now, "Erro")).isFalse();
        assertThat(store.sent(recovered, now)).isTrue();
    }

    @Test
    void naoEnviaCredenciaisExpiradas() {
        register();
        jdbc.update("UPDATE email_outbox SET expires_at = ?", LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1));

        new EmailOutboxWorker(store, cipher, delivery).dispatch();

        assertThat(status()).isEqualTo("EXPIRED");
        assertThat(jdbc.queryForObject("SELECT payload FROM email_outbox", String.class)).isNull();
        verifyNoInteractions(delivery);
    }

    @Test
    void novasCredenciaisCancelamEmailPendenteAnteriorNaMesmaTransacao() {
        register();
        transaction.executeWithoutResult(tx -> adapter.enviarComTemplate("new@example.com", "Acesso",
                "primeiroacesso", Map.of("nomeUsuario", "Teste", "senha", "nova-senha"),
                LocalDateTime.now().plusHours(2)));

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM email_outbox WHERE status = 'PENDING'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM email_outbox WHERE status = 'CANCELLED' AND payload IS NULL",
                Integer.class)).isEqualTo(1);
        new EmailOutboxWorker(store, cipher, delivery).dispatch();
        verify(delivery, times(1)).entregar(any());
    }

    @Test
    void workerNaoEnxergaEmailAntesDoCommit() {
        transaction.executeWithoutResult(tx -> {
            registrarUsuario();
            assertThat(store.claim(LocalDateTime.now(ZoneOffset.UTC))).isEmpty();
        });
        assertThat(store.claim(LocalDateTime.now(ZoneOffset.UTC))).isPresent();
    }

    @Test
    void exclusaoSemConflitoRemoveUsuarioESeguranca() {
        register();

        transaction.executeWithoutResult(tx -> useCase.excluirUsuario("new@example.com"));

        assertThat(count("usuarios")).isZero();
        assertThat(count("seguranca")).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuarios", "seguranca"})
    void exclusaoConcorrenteFalhaRapidoESemExcluirParcialmente(String table) throws Exception {
        register();
        try (var blocker = dataSource.getConnection()) {
            blocker.setAutoCommit(false);
            String column = table.equals("usuarios") ? "login" : "email";
            try (var statement = blocker.prepareStatement("SELECT * FROM " + table + " WHERE " + column + " = ? FOR UPDATE")) {
                statement.setString(1, "new@example.com");
                try (var result = statement.executeQuery()) {
                    assertThat(result.next()).isTrue();
                }
            }

            assertTimeout(Duration.ofSeconds(3), () ->
                    assertThatThrownBy(() -> transaction.executeWithoutResult(tx -> {
                        jdbc.execute("SET LOCK_TIMEOUT 10000");
                        useCase.excluirUsuario("new@example.com");
                    })).isInstanceOf(ConcurrencyFailureException.class));

            assertThat(count("usuarios")).isEqualTo(1);
            assertThat(count("seguranca")).isEqualTo(1);
            assertThat(status()).isEqualTo("PENDING");
            blocker.rollback();
        }
        transaction.executeWithoutResult(tx -> useCase.excluirUsuario("new@example.com"));
        assertThat(count("usuarios")).isZero();
        assertThat(count("seguranca")).isZero();
    }

    @Test
    void primeiroAcessoContinuaAtomicoAposIntroduzirLocks() {
        register();

        transaction.executeWithoutResult(tx -> {
            try {
                useCase.primeiroAcesso("new@example.com");
            } catch (jakarta.mail.MessagingException e) {
                throw new IllegalStateException(e);
            }
        });

        assertThat(count("usuarios")).isEqualTo(1);
        assertThat(count("seguranca")).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM email_outbox WHERE status = 'PENDING'", Integer.class))
                .isEqualTo(1);
    }

    private void register() {
        transaction.executeWithoutResult(tx -> registrarUsuario());
    }

    private void registrarUsuario() {
        try {
            useCase.registrarUsuario(new RegisterDTO("new@example.com", null, UserRole.USER, "Teste", "52998224725"));
        } catch (jakarta.mail.MessagingException e) {
            throw new IllegalStateException(e);
        }
    }

    private int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    private String status() {
        return jdbc.queryForObject("SELECT status FROM email_outbox", String.class);
    }

    @TestConfiguration
    static class Config {
        @Bean ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean SpringTemplateEngine templateEngine() {
            var resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("templates/");
            resolver.setSuffix(".html");
            resolver.setCharacterEncoding("UTF-8");
            var engine = new SpringTemplateEngine();
            engine.setTemplateResolver(resolver);
            return engine;
        }
    }
}
