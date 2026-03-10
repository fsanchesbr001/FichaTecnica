package com.fabriciosanches.fichatecnica.cors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "TRACE", "CONNECT"};
    private static final Logger logger = LogManager.getLogger(CorsConfig.class);

    @Value("${site.origin.allowed:http://localhost:4200,http://localhost:3000}")
    private String ALLOWED_ORIGINS;


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = ALLOWED_ORIGINS.split(",");

        // Trimmar espaços em branco de cada origem
        for (int i = 0; i < origins.length; i++) {
            origins[i] = origins[i].trim();
        }

        logger.info("Configurando CORS para as seguintes origins: {}", String.join(", ", origins));

        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}


