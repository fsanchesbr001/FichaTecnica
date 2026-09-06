package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.HistoricoItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.domains.Produto;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ItemProdutoRepository;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;

import java.util.List;
import java.util.Objects;

public class DeletarItemUseCase implements DeletarItemPort {
    private final ItemRepositoryPort itemRepositoryPort;
    private final HistoricoItemRepositoryPort historicoItemRepositoryPort;
    private final ItemProdutoRepository itemProdutoRepository;
    private final ProdutoRepository produtoRepository;

    public DeletarItemUseCase(
            ItemRepositoryPort itemRepositoryPort,
            HistoricoItemRepositoryPort historicoItemRepositoryPort,
            ItemProdutoRepository itemProdutoRepository,
            ProdutoRepository produtoRepository) {
        this.itemRepositoryPort = Objects.requireNonNull(itemRepositoryPort, "Item repository port não pode ser nulo");
        this.historicoItemRepositoryPort = Objects.requireNonNull(historicoItemRepositoryPort, "HistoricoItem repository port não pode ser nulo");
        this.itemProdutoRepository = Objects.requireNonNull(itemProdutoRepository, "ItemProdutoRepository não pode ser nulo");
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository não pode ser nulo");
    }

    @Override
    public void deletar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id não pode ser nulo");
        }

        Item item = itemRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new FichaTecnicaException("Item com ID " + id + " não encontrado"));

        historicoItemRepositoryPort.deletarPorCodigoItem(id);

        List<ItemProduto> itemProdutoList = itemProdutoRepository.findByItemCodigo(item.getCodigo());
        for (ItemProduto itemProduto : itemProdutoList) {
            Produto produto = itemProduto.getProduto();
            produto.setValorItens(produto.getValorItens().subtract(itemProduto.getValor()));
            produtoRepository.save(produto);
            itemProdutoRepository.deleteItemProduto(itemProduto.getId().getItemId());
        }

        itemRepositoryPort.deletarPorId(item.getCodigo());
    }
}

