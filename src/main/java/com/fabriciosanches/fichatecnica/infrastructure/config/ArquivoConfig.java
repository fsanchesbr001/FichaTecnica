package com.fabriciosanches.fichatecnica.infrastructure.config;

import com.fabriciosanches.fichatecnica.core.ports.in.TransferenciaArquivoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ArmazenamentoArquivoPort;
import com.fabriciosanches.fichatecnica.core.usecase.TransferenciaArquivoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArquivoConfig {

    @Bean
    public TransferenciaArquivoUseCase transferenciaArquivoUseCase(ArmazenamentoArquivoPort armazenamentoArquivoPort) {
        return new TransferenciaArquivoUseCase(armazenamentoArquivoPort);
    }

    @Bean
    public TransferenciaArquivoPort transferenciaArquivoPort(TransferenciaArquivoUseCase transferenciaArquivoUseCase) {
        return transferenciaArquivoUseCase;
    }
}

