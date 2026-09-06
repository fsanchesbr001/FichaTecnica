package com.fabriciosanches.fichatecnica.infrastructure.config;

import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RegistrarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.HistoricoItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.usecase.AtualizarItemUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.BuscarItemUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.CriarItemUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.DeletarItemUseCase;
import com.fabriciosanches.fichatecnica.repository.ItemProdutoRepository;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
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
            ItemProdutoRepository itemProdutoRepository,
            ObterValoresConversaoPort obterValoresConversaoPort,
            ProdutoRepository produtoRepository) {
        return new AtualizarItemUseCase(
                itemRepositoryPort,
                registrarHistoricoItemPort,
                itemProdutoRepository,
                obterValoresConversaoPort,
                produtoRepository
        );
    }

    @Bean
    public DeletarItemPort deletarItemPort(
            ItemRepositoryPort itemRepositoryPort,
            HistoricoItemRepositoryPort historicoItemRepositoryPort,
            ItemProdutoRepository itemProdutoRepository,
            ProdutoRepository produtoRepository) {
        return new DeletarItemUseCase(itemRepositoryPort, historicoItemRepositoryPort, itemProdutoRepository, produtoRepository);
    }
}

