package com.fabriciosanches.fichatecnica.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.swagger.v3.oas.models.OpenAPI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = OpenApiConfigTest.TestApplication.class)
@ActiveProfiles("test")
class OpenApiConfigTest {

    @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    static class TestApplication { }

    @Autowired
    private OpenAPI openAPI;

    @Test
    void fichaTecnicaOpenAPI_DeveConfigurarTituloESeguranca() {
        assertNotNull(openAPI);
        assertEquals("Ficha Técnica API", openAPI.getInfo().getTitle());
        assertEquals("1.9.8", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get(OpenApiConfig.SECURITY_SCHEME_NAME));
    }
}

