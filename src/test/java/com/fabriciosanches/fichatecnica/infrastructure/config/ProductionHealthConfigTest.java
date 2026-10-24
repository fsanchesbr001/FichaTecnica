package com.fabriciosanches.fichatecnica.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.mail.MailHealthContributorAutoConfiguration;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.sql.DataSource;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ProductionHealthConfigTest {

    private final JavaMailSenderImpl mailSender = mock(JavaMailSenderImpl.class);
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MailHealthContributorAutoConfiguration.class,
                    DataSourceHealthContributorAutoConfiguration.class))
            .withBean(JavaMailSenderImpl.class, () -> mailSender)
            .withBean(DataSource.class, () -> mock(DataSource.class));

    @Test
    void production_DeveDesabilitarHealthDoEmailEPreservarBancoESender() throws IOException {
        ResourcePropertySource productionProperties =
                new ResourcePropertySource("classpath:application-production.properties");
        contextRunner.withInitializer(context -> context.getEnvironment().getPropertySources()
                        .addLast(productionProperties))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean("mailHealthContributor");
                    assertThat(context).hasBean("dbHealthContributor");
                    assertThat(context.getBean("dbHealthContributor")).isInstanceOf(HealthContributor.class);
                    assertThat(context.getBean(JavaMailSenderImpl.class)).isSameAs(mailSender);
                    verifyNoInteractions(mailSender);
                });
    }

    @Test
    void semConfiguracaoDeProducao_DeveManterHealthDoEmail() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasBean("mailHealthContributor");
            assertThat(context).hasBean("dbHealthContributor");
        });
    }
}
