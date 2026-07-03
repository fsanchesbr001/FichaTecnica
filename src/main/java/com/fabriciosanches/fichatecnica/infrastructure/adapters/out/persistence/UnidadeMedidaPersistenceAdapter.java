package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UnidadeMedidaPersistenceAdapter implements UnidadeMedidaRepositoryPort {
    private final SpringDataUnidadeMedidaRepository repository;

    public UnidadeMedidaPersistenceAdapter(SpringDataUnidadeMedidaRepository repository) {
        this.repository = repository;
    }

    @Override
    public UnidadeMedida salvar(UnidadeMedida unidadeMedida) {
        UnidadeMedidaEntity entidade = toEntity(unidadeMedida);
        UnidadeMedidaEntity salva = repository.save(entidade);
        return toDomain(salva);
    }

    @Override
    public List<UnidadeMedida> buscarTodos() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<UnidadeMedida> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<UnidadeMedida> buscarPorSigla(String sigla) {
        return repository.findBySigla(sigla).map(this::toDomain);
    }

    @Override
    public void deletar(Long id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("FKC-Registro não pode ser deletado. Existem Conversões vinculadas.");
        }
    }

    private UnidadeMedida toDomain(UnidadeMedidaEntity entidade) {
        return new UnidadeMedida(entidade.getCodigo(), entidade.getNome(), entidade.getSigla());
    }

    private UnidadeMedidaEntity toEntity(UnidadeMedida domain) {
        return new UnidadeMedidaEntity(domain.getCodigo(), domain.getNome(), domain.getSigla());
    }
}
