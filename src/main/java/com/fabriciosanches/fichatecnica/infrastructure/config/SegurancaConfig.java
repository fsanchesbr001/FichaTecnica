package com.fabriciosanches.fichatecnica.infrastructure.config;

import com.fabriciosanches.fichatecnica.core.ports.in.AutenticarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ControleAcessoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RecuperacaoSenhaPort;
import com.fabriciosanches.fichatecnica.core.ports.out.EnviarEmailPort;
import com.fabriciosanches.fichatecnica.core.ports.out.GeradorTokenPort;
import com.fabriciosanches.fichatecnica.core.ports.out.SegurancaRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UsuarioRepositoryPort;
import com.fabriciosanches.fichatecnica.core.usecase.AutenticacaoUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.SegurancaUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SegurancaConfig {

    @Bean
    public SegurancaUseCase segurancaUseCase(SegurancaRepositoryPort segurancaRepositoryPort,
                                             UsuarioRepositoryPort usuarioRepositoryPort,
                                             EnviarEmailPort enviarEmailPort) {
        return new SegurancaUseCase(segurancaRepositoryPort, usuarioRepositoryPort, enviarEmailPort);
    }

    @Bean
    public RecuperacaoSenhaPort recuperacaoSenhaPort(@Qualifier("segurancaUseCase") SegurancaUseCase segurancaUseCase) {
        return segurancaUseCase;
    }

    @Bean
    public ControleAcessoPort controleAcessoPort(@Qualifier("segurancaUseCase") SegurancaUseCase segurancaUseCase) {
        return segurancaUseCase;
    }

    @Bean
    public AutenticacaoUseCase autenticacaoUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                                   GeradorTokenPort geradorTokenPort) {
        return new AutenticacaoUseCase(usuarioRepositoryPort, geradorTokenPort);
    }

    @Bean
    public AutenticarUsuarioPort autenticarUsuarioPort(@Qualifier("autenticacaoUseCase") AutenticacaoUseCase autenticacaoUseCase) {
        return autenticacaoUseCase;
    }

    @Bean
    public GerarTokenPort gerarTokenPort(@Qualifier("autenticacaoUseCase") AutenticacaoUseCase autenticacaoUseCase) {
        return autenticacaoUseCase;
    }
}

