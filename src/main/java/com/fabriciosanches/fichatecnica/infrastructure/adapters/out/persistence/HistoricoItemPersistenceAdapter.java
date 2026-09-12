package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.HistoricoItem;
import com.fabriciosanches.fichatecnica.core.ports.out.HistoricoItemRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class HistoricoItemPersistenceAdapter implements HistoricoItemRepositoryPort {
    private final SpringDataHistoricoItemRepository repository;

    public HistoricoItemPersistenceAdapter(SpringDataHistoricoItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public HistoricoItem salvar(HistoricoItem historicoItem) {
        HistoricoItemEntity entidade = toEntity(historicoItem);
        HistoricoItemEntity salvo = repository.save(entidade);
        return toDomain(salvo);
    }

    @Override
    public List<HistoricoItem> buscarTodos() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<HistoricoItem> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<HistoricoItem> buscarPorCodigoItem(Long codigoItem) {
        return repository.findByCdItem(codigoItem).stream().map(this::toDomain).toList();
    }

    @Override
    public List<HistoricoItem> buscarPorCodigoItemOrdenadoPorDataInicio(Long codigoItem) {
        return repository.findByCdItemOrderByCdItemAscDataInicioAscCodigoAsc(codigoItem).stream().map(this::toDomain).toList();
    }

    @Override
    public void deletarPorCodigoItem(Long codigoItem) {
        repository.deleteHistoricoItemByCdItem(codigoItem);
    }

    private HistoricoItem toDomain(HistoricoItemEntity entidade) {
        return new HistoricoItem(entidade.getCodigo(), entidade.getCdItem(), entidade.getValor(), entidade.getDataInicio());
    }

    private HistoricoItemEntity toEntity(HistoricoItem historicoItem) {
        return new HistoricoItemEntity(
                historicoItem.getCodigo(),
                historicoItem.getCdItem(),
                historicoItem.getValor(),
                historicoItem.getDataInicio()
        );
    }
}


