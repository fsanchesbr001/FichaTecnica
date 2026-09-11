package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.Seguranca;
import com.fabriciosanches.fichatecnica.core.ports.out.SegurancaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SegurancaPersistenceAdapter implements SegurancaRepositoryPort {

    private final SpringDataSegurancaRepository repository;

    public SegurancaPersistenceAdapter(SpringDataSegurancaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Seguranca> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public String buscarCpfPorEmail(String email) {
        return repository.findCPFByEmail(email);
    }

    @Override
    public Seguranca salvar(Seguranca seguranca) {
        SegurancaEntity salva = repository.save(toEntity(seguranca));
        return toDomain(salva);
    }

    @Override
    public void deletar(Seguranca seguranca) {
        repository.delete(toEntity(seguranca));
    }

    @Override
    public List<Seguranca> buscarTodos() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private Seguranca toDomain(SegurancaEntity entity) {
        return new Seguranca(
                entity.getCodigo(),
                entity.getCpf(),
                entity.getEmail(),
                entity.getTokenSeguranca(),
                entity.getTentativas(),
                entity.getBloqueado_admin(),
                entity.getBloqueado_tentativas(),
                entity.getBloqueado_expiracao(),
                entity.getPrimeiro_acesso(),
                entity.getDataCriacao(),
                entity.getDataExpiracaoSenha(),
                entity.getDataExpiracaoToken()
        );
    }

    private SegurancaEntity toEntity(Seguranca domain) {
        return new SegurancaEntity(
                domain.getCodigo(),
                domain.getCpf(),
                domain.getEmail(),
                domain.getTokenSeguranca(),
                domain.getTentativas(),
                domain.getBloqueado_admin(),
                domain.getBloqueado_tentativas(),
                domain.getBloqueado_expiracao(),
                domain.getPrimeiro_acesso(),
                domain.getDataCriacao(),
                domain.getDataExpiracaoSenha(),
                domain.getDataExpiracaoToken()
        );
    }
}

