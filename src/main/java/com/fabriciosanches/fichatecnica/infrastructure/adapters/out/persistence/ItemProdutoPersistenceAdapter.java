package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.ItemProdutoId;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemProdutoRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ItemProdutoPersistenceAdapter implements ItemProdutoRepositoryPort {
    private final SpringDataItemProdutoRepository repository;

    public ItemProdutoPersistenceAdapter(SpringDataItemProdutoRepository repository) {
        this.repository = repository;
    }

    @Override
    public ItemProduto salvar(ItemProduto itemProduto) {
        ItemProdutoEntity salvo = repository.save(toEntity(itemProduto));
        return toDomain(salvo);
    }

    @Override
    public List<ItemProduto> buscarTodos() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ItemProduto> buscarPorId(ItemProdutoId id) {
        return repository.findById(new ItemProdutoIdEntity(id.getProdutoId(), id.getItemId())).map(this::toDomain);
    }

    @Override
    public List<ItemProduto> buscarPorProdutoId(Long produtoId) {
        return repository.findByProdutoCodigo(produtoId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ItemProduto> buscarPorItemId(Long itemId) {
        return repository.findByItemCodigo(itemId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ItemProduto> buscarPorProdutoIdEItemId(Long produtoId, Long itemId) {
        return repository.findByProdutoCodigoAndItemCodigo(produtoId, itemId).map(this::toDomain);
    }

    @Override
    public void deletarPorProdutoIdEItemId(Long produtoId, Long itemId) {
        repository.deleteByProdutoCodigoAndItemCodigo(produtoId, itemId);
    }

    private ItemProduto toDomain(ItemProdutoEntity entidade) {
        return new ItemProduto(
                entidade.getId() == null ? null : new ItemProdutoId(entidade.getId().getProdutoId(), entidade.getId().getItemId()),
                entidade.getItem() == null ? null : new Item(
                        entidade.getItem().getCodigo(),
                        entidade.getItem().getNome(),
                        entidade.getItem().getUnidadeMedida() == null ? null : new UnidadeMedida(
                                entidade.getItem().getUnidadeMedida().getCodigo(),
                                entidade.getItem().getUnidadeMedida().getNome(),
                                entidade.getItem().getUnidadeMedida().getSigla()),
                        entidade.getItem().getValor()),
                entidade.getProduto() == null ? null : new Produto(
                        entidade.getProduto().getCodigo(),
                        entidade.getProduto().getNome(),
                        entidade.getProduto().getDescricao(),
                        entidade.getProduto().getImagem(),
                        entidade.getProduto().getValorVenda(),
                        entidade.getProduto().getValorItens(),
                        List.of()),
                entidade.getUnidadePara() == null ? null : new UnidadeMedida(
                        entidade.getUnidadePara().getCodigo(),
                        entidade.getUnidadePara().getNome(),
                        entidade.getUnidadePara().getSigla()),
                entidade.getQuantidade(),
                entidade.getValor());
    }

    private ItemProdutoEntity toEntity(ItemProduto dominio) {
        return new ItemProdutoEntity(
                dominio.getId() == null ? null : new ItemProdutoIdEntity(dominio.getId().getProdutoId(), dominio.getId().getItemId()),
                toItemEntity(dominio.getItem()),
                toProdutoEntity(dominio.getProduto()),
                toUnidadeEntity(dominio.getUnidadePara()),
                dominio.getQuantidade(),
                dominio.getValor());
    }

    private ItemEntity toItemEntity(Item item) {
        if (item == null) {
            return null;
        }
        UnidadeMedida unidade = item.getUnidadeMedida();
        return new ItemEntity(
                item.getCodigo(),
                item.getNome(),
                unidade == null ? null : new UnidadeMedidaEntity(unidade.getCodigo(), unidade.getNome(), unidade.getSigla()),
                item.getValor());
    }

    private ProdutoEntity toProdutoEntity(Produto produto) {
        if (produto == null) {
            return null;
        }
        return new ProdutoEntity(
                produto.getCodigo(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getImagem(),
                produto.getValorVenda(),
                produto.getValorItens(),
                null);
    }

    private UnidadeMedidaEntity toUnidadeEntity(UnidadeMedida unidadeMedida) {
        if (unidadeMedida == null) {
            return null;
        }
        return new UnidadeMedidaEntity(unidadeMedida.getCodigo(), unidadeMedida.getNome(), unidadeMedida.getSigla());
    }
}

