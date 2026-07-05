package com.fabriciosanches.fichatecnica.infrastructure.config;

import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioDetalheUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioListaUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.GeradorRelatorioUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;
import com.fabriciosanches.fichatecnica.core.usecase.AtualizarUnidadeMedidaUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.BuscarUnidadeMedidaUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.CriarUnidadeMedidaUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.DeletarUnidadeMedidaUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.GerarRelatorioDetalheUnidadeMedidaUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.GerarRelatorioListaUnidadeMedidaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UnidadeMedidaConfig {

    @Bean
    public CriarUnidadeMedidaPort criarUnidadeMedidaPort(UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort) {
        return new CriarUnidadeMedidaUseCase(unidadeMedidaRepositoryPort);
    }

    @Bean
    public AtualizarUnidadeMedidaPort atualizarUnidadeMedidaPort(UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort) {
        return new AtualizarUnidadeMedidaUseCase(unidadeMedidaRepositoryPort);
    }

    @Bean
    public BuscarUnidadeMedidaPort buscarUnidadeMedidaPort(UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort) {
        return new BuscarUnidadeMedidaUseCase(unidadeMedidaRepositoryPort);
    }

    @Bean
    public DeletarUnidadeMedidaPort deletarUnidadeMedidaPort(UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort) {
        return new DeletarUnidadeMedidaUseCase(unidadeMedidaRepositoryPort);
    }

    @Bean
    public GerarRelatorioListaUnidadeMedidaPort gerarRelatorioListaUnidadeMedidaPort(
            UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort,
            GeradorRelatorioUnidadeMedidaPort geradorRelatorioUnidadeMedidaPort) {
        return new GerarRelatorioListaUnidadeMedidaUseCase(unidadeMedidaRepositoryPort, geradorRelatorioUnidadeMedidaPort);
    }

    @Bean
    public GerarRelatorioDetalheUnidadeMedidaPort gerarRelatorioDetalheUnidadeMedidaPort(
            UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort,
            GeradorRelatorioUnidadeMedidaPort geradorRelatorioUnidadeMedidaPort) {
        return new GerarRelatorioDetalheUnidadeMedidaUseCase(unidadeMedidaRepositoryPort, geradorRelatorioUnidadeMedidaPort);
    }
}
