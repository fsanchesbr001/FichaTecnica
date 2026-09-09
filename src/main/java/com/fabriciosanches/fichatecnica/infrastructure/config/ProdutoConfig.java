package com.fabriciosanches.fichatecnica.infrastructure.config;

import com.fabriciosanches.fichatecnica.core.ports.in.BuscarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ConsultarUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.IniciarUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarJobsUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RemoverImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoImagemStoragePort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.usecase.ProdutoImagemUploadUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.ProdutoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProdutoConfig {

    @Bean
    public ProdutoUseCase produtoUseCase(ProdutoRepositoryPort produtoRepositoryPort) {
        return new ProdutoUseCase(produtoRepositoryPort);
    }

    @Bean
    public CriarProdutoPort criarProdutoPort(ProdutoUseCase produtoUseCase) {
        return produtoUseCase;
    }

    @Bean
    public BuscarProdutoPort buscarProdutoPort(ProdutoUseCase produtoUseCase) {
        return produtoUseCase;
    }

    @Bean
    public com.fabriciosanches.fichatecnica.core.ports.in.AtualizarProdutoPort atualizarProdutoPort(ProdutoUseCase produtoUseCase) {
        return produtoUseCase;
    }

    @Bean
    public DeletarProdutoPort deletarProdutoPort(ProdutoUseCase produtoUseCase) {
        return produtoUseCase;
    }

    @Bean
    public ProdutoImagemUploadUseCase produtoImagemUploadUseCase(
            ProdutoRepositoryPort produtoRepositoryPort,
            ProdutoImagemStoragePort produtoImagemStoragePort) {
        return new ProdutoImagemUploadUseCase(produtoRepositoryPort, produtoImagemStoragePort);
    }

    @Bean
    public IniciarUploadImagemProdutoPort iniciarUploadImagemProdutoPort(ProdutoImagemUploadUseCase useCase) {
        return useCase;
    }

    @Bean
    public ConsultarUploadImagemProdutoPort consultarUploadImagemProdutoPort(ProdutoImagemUploadUseCase useCase) {
        return useCase;
    }

    @Bean
    public RemoverImagemProdutoPort removerImagemProdutoPort(ProdutoImagemUploadUseCase useCase) {
        return useCase;
    }

    @Bean
    public ListarJobsUploadImagemProdutoPort listarJobsUploadImagemProdutoPort(ProdutoImagemUploadUseCase useCase) {
        return useCase;
    }
}

