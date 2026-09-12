package com.fabriciosanches.fichatecnica.infrastructure.config;

import com.fabriciosanches.fichatecnica.core.ports.in.AdicionarItemAoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarQuantidadeItemDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CalcularValoresItensProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPizzaProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarItensDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarProdutosPorItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterDescricoesUnidadePort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RemoverItemDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;
import com.fabriciosanches.fichatecnica.core.usecase.ItensProdutoUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ItemProdutoConfig {

    @Bean
    public ItensProdutoUseCase itensProdutoUseCase(
            ItemProdutoRepositoryPort itemProdutoRepositoryPort,
            ProdutoRepositoryPort produtoRepositoryPort,
            ItemRepositoryPort itemRepositoryPort,
            UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort,
            ObterValoresConversaoPort obterValoresConversaoPort) {
        return new ItensProdutoUseCase(
                itemProdutoRepositoryPort,
                produtoRepositoryPort,
                itemRepositoryPort,
                unidadeMedidaRepositoryPort,
                obterValoresConversaoPort);
    }

    @Bean
    public AdicionarItemAoProdutoPort adicionarItemAoProdutoPort(
            @Qualifier("itensProdutoUseCase") ItensProdutoUseCase useCase) {
        return useCase;
    }

    @Bean
    public ListarItensDoProdutoPort listarItensDoProdutoPort(
            @Qualifier("itensProdutoUseCase") ItensProdutoUseCase useCase) {
        return useCase;
    }

    @Bean
    public ListarProdutosPorItemPort listarProdutosPorItemPort(
            @Qualifier("itensProdutoUseCase") ItensProdutoUseCase useCase) {
        return useCase;
    }

    @Bean
    public CalcularValoresItensProdutoPort calcularValoresItensProdutoPort(
            @Qualifier("itensProdutoUseCase") ItensProdutoUseCase useCase) {
        return useCase;
    }

    @Bean
    public RemoverItemDoProdutoPort removerItemDoProdutoPort(
            @Qualifier("itensProdutoUseCase") ItensProdutoUseCase useCase) {
        return useCase;
    }

    @Bean
    public AtualizarQuantidadeItemDoProdutoPort atualizarQuantidadeItemDoProdutoPort(
            @Qualifier("itensProdutoUseCase") ItensProdutoUseCase useCase) {
        return useCase;
    }

    @Bean
    public ObterDescricoesUnidadePort obterDescricoesUnidadePort(
            @Qualifier("itensProdutoUseCase") ItensProdutoUseCase useCase) {
        return useCase;
    }

    @Bean
    public GerarGraficoPizzaProdutoPort gerarGraficoPizzaProdutoPort(
            @Qualifier("itensProdutoUseCase") ItensProdutoUseCase useCase) {
        return useCase;
    }
}

