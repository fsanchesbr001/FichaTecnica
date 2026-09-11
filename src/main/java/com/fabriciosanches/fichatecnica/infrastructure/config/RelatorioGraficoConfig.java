package com.fabriciosanches.fichatecnica.infrastructure.config;

import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioPort;
import com.fabriciosanches.fichatecnica.core.usecase.GerarGraficoUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.GerarRelatorioUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelatorioGraficoConfig {

    @Bean
    public GerarRelatorioUseCase gerarRelatorioUseCase() {
        return new GerarRelatorioUseCase();
    }

    @Bean
    public GerarGraficoUseCase gerarGraficoUseCase() {
        return new GerarGraficoUseCase();
    }

    @Bean
    public GerarRelatorioPort gerarRelatorioPort(GerarRelatorioUseCase useCase) {
        return useCase;
    }

    @Bean
    public GerarGraficoPort gerarGraficoPort(GerarGraficoUseCase useCase) {
        return useCase;
    }
}

