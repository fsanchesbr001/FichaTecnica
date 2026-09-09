package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.ItemProdutoId;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProdutoPersistenceAdapter implements ProdutoRepositoryPort {
    private final SpringDataProdutoRepository repository;

    public ProdutoPersistenceAdapter(SpringDataProdutoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Produto salvar(Produto produto) {
        ProdutoEntity salvo = repository.save(toEntity(produto));
        return toDomain(salvo);
    }

    @Override
    public List<Produto> buscarTodos() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public long contarPorNome(String nome) {
        return repository.countByName(nome);
    }

    @Override
    public void deletarPorId(Long id) {
        repository.deleteById(id);
    }

    private Produto toDomain(ProdutoEntity entidade) {
        return new Produto(
                entidade.getCodigo(),
                entidade.getNome(),
                entidade.getDescricao(),
                entidade.getImagem(),
                entidade.getValorVenda(),
                entidade.getValorItens(),
                entidade.getProdutosList() == null ? List.of() : entidade.getProdutosList().stream()
                        .map(this::toDomainItemProduto)
                        .toList());
    }

    private ProdutoEntity toEntity(Produto dominio) {
        return new ProdutoEntity(
                dominio.getCodigo(),
                dominio.getNome(),
                dominio.getDescricao(),
                dominio.getImagem(),
                dominio.getValorVenda(),
                dominio.getValorItens(),
                null);
    }

    private ItemProduto toDomainItemProduto(ItemProdutoEntity entidade) {
        return new ItemProduto(
                new ItemProdutoId(
                        entidade.getId() != null ? entidade.getId().getProdutoId() : null,
                        entidade.getId() != null ? entidade.getId().getItemId() : null),
                entidade.getItem() == null ? null : new com.fabriciosanches.fichatecnica.core.domain.Item(
                        entidade.getItem().getCodigo(),
                        entidade.getItem().getNome(),
                        entidade.getItem().getUnidadeMedida(),
                        entidade.getItem().getValor()),
                entidade.getProduto() == null ? null : new Produto(
                        entidade.getProduto().getCodigo(),
                        entidade.getProduto().getNome(),
                        entidade.getProduto().getDescricao(),
                        entidade.getProduto().getImagem(),
                        entidade.getProduto().getValorVenda(),
                        entidade.getProduto().getValorItens(),
                        List.of()),
                entidade.getUnidadePara() == null ? null : new com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida(
                        entidade.getUnidadePara().getCodigo(),
                        entidade.getUnidadePara().getNome(),
                        entidade.getUnidadePara().getSigla()),
                entidade.getQuantidade(),
                entidade.getValor());
    }
}

