package com.fabriciosanches.fichatecnica.infrastructure.config;

import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ExcluirUsuarioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerenciarBloqueioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.PrimeiroAcessoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.SegurancaRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UsuarioRepositoryPort;
import com.fabriciosanches.fichatecnica.core.usecase.SegurancaUseCase;
import com.fabriciosanches.fichatecnica.core.usecase.UsuarioUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioConfig {

    @Bean
    public UsuarioUseCase usuarioUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                         SegurancaRepositoryPort segurancaRepositoryPort,
                                         SegurancaUseCase segurancaUseCase) {
        return new UsuarioUseCase(usuarioRepositoryPort, segurancaRepositoryPort, segurancaUseCase);
    }

    @Bean
    public CriarUsuarioPort criarUsuarioPort(@Qualifier("usuarioUseCase") UsuarioUseCase usuarioUseCase) {
        return usuarioUseCase;
    }

    @Bean
    public BuscarUsuarioPort buscarUsuarioPort(@Qualifier("usuarioUseCase") UsuarioUseCase usuarioUseCase) {
        return usuarioUseCase;
    }

    @Bean
    public AtualizarUsuarioPort atualizarUsuarioPort(@Qualifier("usuarioUseCase") UsuarioUseCase usuarioUseCase) {
        return usuarioUseCase;
    }

    @Bean
    public ExcluirUsuarioPort excluirUsuarioPort(@Qualifier("usuarioUseCase") UsuarioUseCase usuarioUseCase) {
        return usuarioUseCase;
    }

    @Bean
    public PrimeiroAcessoPort primeiroAcessoPort(@Qualifier("usuarioUseCase") UsuarioUseCase usuarioUseCase) {
        return usuarioUseCase;
    }

    @Bean
    public GerenciarBloqueioPort gerenciarBloqueioPort(@Qualifier("usuarioUseCase") UsuarioUseCase usuarioUseCase) {
        return usuarioUseCase;
    }
}
