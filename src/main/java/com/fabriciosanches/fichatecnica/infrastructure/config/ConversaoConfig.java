package com.fabriciosanches.fichatecnica.infrastructure.config;

import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.usecase.AtualizarConversaoUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.BuscarConversaoUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.CriarConversaoUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.DeletarConversaoUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.GerarRelatorioConversaoUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.ObterValoresConversaoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConversaoConfig {

    @Bean
    public CriarConversaoPort criarConversaoPort(ConversaoRepositoryPort conversaoRepositoryPort) {
        return new CriarConversaoUseCase(conversaoRepositoryPort);
    }

    @Bean
    public BuscarConversaoPort buscarConversaoPort(ConversaoRepositoryPort conversaoRepositoryPort) {
        return new BuscarConversaoUseCase(conversaoRepositoryPort);
    }

    @Bean
    public AtualizarConversaoPort atualizarConversaoPort(ConversaoRepositoryPort conversaoRepositoryPort) {
        return new AtualizarConversaoUseCase(conversaoRepositoryPort);
    }

    @Bean
    public DeletarConversaoPort deletarConversaoPort(ConversaoRepositoryPort conversaoRepositoryPort) {
        return new DeletarConversaoUseCase(conversaoRepositoryPort);
    }

    @Bean
    public GerarRelatorioConversaoPort gerarRelatorioConversaoPort(ConversaoRepositoryPort conversaoRepositoryPort) {
        return new GerarRelatorioConversaoUseCase(conversaoRepositoryPort);
    }

    @Bean
    public ObterValoresConversaoPort obterValoresConversaoPort(ConversaoRepositoryPort conversaoRepositoryPort) {
        return new ObterValoresConversaoUseCase(conversaoRepositoryPort);
    }
}
