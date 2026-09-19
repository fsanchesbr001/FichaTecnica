package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.HistoricoItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DeletarItemUseCase implements DeletarItemPort {
    private final ItemRepositoryPort itemRepositoryPort;
    private final HistoricoItemRepositoryPort historicoItemRepositoryPort;
    private final ItemProdutoRepositoryPort itemProdutoRepositoryPort;
    private final ProdutoRepositoryPort produtoRepositoryPort;

    public DeletarItemUseCase(
            ItemRepositoryPort itemRepositoryPort,
            HistoricoItemRepositoryPort historicoItemRepositoryPort,
            ItemProdutoRepositoryPort itemProdutoRepositoryPort,
            ProdutoRepositoryPort produtoRepositoryPort) {
        this.itemRepositoryPort = Objects.requireNonNull(itemRepositoryPort, "Item repository port não pode ser nulo");
        this.historicoItemRepositoryPort = Objects.requireNonNull(historicoItemRepositoryPort, "HistoricoItem repository port não pode ser nulo");
        this.itemProdutoRepositoryPort = Objects.requireNonNull(itemProdutoRepositoryPort, "ItemProdutoRepositoryPort não pode ser nulo");
        this.produtoRepositoryPort = Objects.requireNonNull(produtoRepositoryPort, "ProdutoRepositoryPort não pode ser nulo");
    }

    @Override
    public void deletar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id não pode ser nulo");
        }

        Item item = itemRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new FichaTecnicaException("Item com ID " + id + " não encontrado"));

        historicoItemRepositoryPort.deletarPorCodigoItem(id);

        List<ItemProduto> itemProdutoList = itemProdutoRepositoryPort.buscarPorItemId(item.getCodigo());
        for (ItemProduto itemProduto : itemProdutoList) {
            Produto produto = itemProduto.getProduto() != null
                    ? itemProduto.getProduto()
                    : produtoRepositoryPort.buscarPorId(itemProduto.getId().getProdutoId())
                    .orElseThrow(() -> new FichaTecnicaException("Produto não encontrado"));
            BigDecimal valorAtual = produto.getValorItens() != null ? produto.getValorItens() : BigDecimal.ZERO;
            BigDecimal valorItem = itemProduto.getValor() != null ? itemProduto.getValor() : BigDecimal.ZERO;
            produto.setValorItens(valorAtual.subtract(valorItem));
            produtoRepositoryPort.salvar(produto);
            itemProdutoRepositoryPort.deletarPorProdutoIdEItemId(itemProduto.getId().getProdutoId(), itemProduto.getId().getItemId());
        }

        itemRepositoryPort.deletarPorId(item.getCodigo());
    }
}


