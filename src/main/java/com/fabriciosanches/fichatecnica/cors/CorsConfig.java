package com.fabriciosanches.fichatecnica.cors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "TRACE", "CONNECT"};
    private static final Logger logger = LogManager.getLogger(CorsConfig.class);

    @Value("${site.origin.allowed:http://localhost:4200,http://localhost:3000}")
    private String ALLOWED_ORIGINS;

    /**
     * Bean usado pelo Spring Security (via Customizer.withDefaults()) para resolver
     * a configuração CORS antes que o SecurityFilterChain processe a requisição.
     * Sem este bean, o cors(Customizer.withDefaults()) não encontra a configuração
     * e o header Access-Control-Allow-Origin nunca é adicionado.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Arrays.stream(ALLOWED_ORIGINS.split(","))
                .map(String::trim)
                .toList();

        logger.info("Registrando CorsConfigurationSource para origins: {}", origins);

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(Arrays.asList(ALLOWED_METHODS));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

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
