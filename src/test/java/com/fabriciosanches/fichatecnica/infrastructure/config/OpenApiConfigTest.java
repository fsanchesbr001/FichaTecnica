package com.fabriciosanches.fichatecnica.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

    @Test
    void fichaTecnicaOpenAPI_DeveConfigurarTituloESeguranca() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.fichaTecnicaOpenAPI();

        assertNotNull(openAPI);
        assertEquals("Ficha T\u00e9cnica API", openAPI.getInfo().getTitle());
        assertEquals("1.9.8", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get(OpenApiConfig.SECURITY_SCHEME_NAME));
    }
}

