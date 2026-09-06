package com.fabriciosanches.fichatecnica.infrastructure.config;

import com.fabriciosanches.fichatecnica.core.ports.in.ListarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RegistrarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.HistoricoItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.usecase.ListarHistoricoItemUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.RegistrarHistoricoItemUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HistoricoItemConfig {

    @Bean
    public RegistrarHistoricoItemPort registrarHistoricoItemPort(HistoricoItemRepositoryPort historicoItemRepositoryPort) {
        return new RegistrarHistoricoItemUseCase(historicoItemRepositoryPort);
    }

    @Bean
    public ListarHistoricoItemPort listarHistoricoItemPort(
            HistoricoItemRepositoryPort historicoItemRepositoryPort,
            ItemRepositoryPort itemRepositoryPort) {
        return new ListarHistoricoItemUseCase(historicoItemRepositoryPort, itemRepositoryPort);
    }
}

