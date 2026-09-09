package com.fabriciosanches.fichatecnica.infrastructure.config;

import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RegistrarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.HistoricoItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.usecase.AtualizarItemUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.BuscarItemUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.CriarItemUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.DeletarItemUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ItemConfig {

    @Bean
    public CriarItemPort criarItemPort(ItemRepositoryPort itemRepositoryPort, RegistrarHistoricoItemPort registrarHistoricoItemPort) {
        return new CriarItemUseCase(itemRepositoryPort, registrarHistoricoItemPort);
    }

    @Bean
    public BuscarItemPort buscarItemPort(ItemRepositoryPort itemRepositoryPort) {
        return new BuscarItemUseCase(itemRepositoryPort);
    }

    @Bean
    public AtualizarItemPort atualizarItemPort(
            ItemRepositoryPort itemRepositoryPort,
            RegistrarHistoricoItemPort registrarHistoricoItemPort,
            ItemProdutoRepositoryPort itemProdutoRepositoryPort,
            ObterValoresConversaoPort obterValoresConversaoPort,
            ProdutoRepositoryPort produtoRepositoryPort) {
        return new AtualizarItemUseCase(
                itemRepositoryPort,
                registrarHistoricoItemPort,
                itemProdutoRepositoryPort,
                obterValoresConversaoPort,
                produtoRepositoryPort
        );
    }

    @Bean
    public DeletarItemPort deletarItemPort(
            ItemRepositoryPort itemRepositoryPort,
            HistoricoItemRepositoryPort historicoItemRepositoryPort,
            ItemProdutoRepositoryPort itemProdutoRepositoryPort,
            ProdutoRepositoryPort produtoRepositoryPort) {
        return new DeletarItemUseCase(itemRepositoryPort, historicoItemRepositoryPort, itemProdutoRepositoryPort, produtoRepositoryPort);
    }
}

