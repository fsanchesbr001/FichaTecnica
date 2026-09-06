package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ItemPersistenceAdapter implements ItemRepositoryPort {
    private final SpringDataItemRepository repository;

    public ItemPersistenceAdapter(SpringDataItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public Item salvar(Item item) {
        ItemEntity entidade = toEntity(item);
        ItemEntity salvo = repository.save(entidade);
        return toDomain(salvo);
    }

    @Override
    public List<Item> buscarTodos() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Item> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public long contarPorNome(String nome) {
        return repository.countByName(nome);
    }

    @Override
    public void deletarPorId(Long id) {
        repository.deleteItem(id);
    }

    private Item toDomain(ItemEntity entidade) {
        return new Item(entidade.getCodigo(), entidade.getNome(), entidade.getUnidadeMedida(), entidade.getValor());
    }

    private ItemEntity toEntity(Item item) {
        return new ItemEntity(item.getCodigo(), item.getNome(), item.getUnidadeMedida(), item.getValor());
    }
}

