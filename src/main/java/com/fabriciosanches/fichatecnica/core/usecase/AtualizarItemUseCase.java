package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RegistrarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.QuantidadeValorDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class AtualizarItemUseCase implements AtualizarItemPort {
    private final ItemRepositoryPort itemRepositoryPort;
    private final RegistrarHistoricoItemPort registrarHistoricoItemPort;
    private final ItemProdutoRepositoryPort itemProdutoRepositoryPort;
    private final ObterValoresConversaoPort obterValoresConversaoPort;
    private final ProdutoRepositoryPort produtoRepositoryPort;

    public AtualizarItemUseCase(
            ItemRepositoryPort itemRepositoryPort,
            RegistrarHistoricoItemPort registrarHistoricoItemPort,
            ItemProdutoRepositoryPort itemProdutoRepositoryPort,
            ObterValoresConversaoPort obterValoresConversaoPort,
            ProdutoRepositoryPort produtoRepositoryPort) {
        this.itemRepositoryPort = Objects.requireNonNull(itemRepositoryPort, "Item repository port não pode ser nulo");
        this.registrarHistoricoItemPort = Objects.requireNonNull(registrarHistoricoItemPort, "Port de histórico não pode ser nulo");
        this.itemProdutoRepositoryPort = Objects.requireNonNull(itemProdutoRepositoryPort, "ItemProdutoRepositoryPort não pode ser nulo");
        this.obterValoresConversaoPort = Objects.requireNonNull(obterValoresConversaoPort, "ObterValoresConversaoPort não pode ser nulo");
        this.produtoRepositoryPort = Objects.requireNonNull(produtoRepositoryPort, "ProdutoRepositoryPort não pode ser nulo");
    }

    @Override
    public Item atualizar(Long id, String nome, UnidadeMedida unidadeMedida, BigDecimal valor) {
        if (id == null) {
            throw new IllegalArgumentException("Id não pode ser nulo");
        }
        Objects.requireNonNull(nome, "Nome do item não pode ser nulo");
        Objects.requireNonNull(unidadeMedida, "Unidade de medida não pode ser nula");
        Objects.requireNonNull(valor, "Valor do item não pode ser nulo");

        Item item = itemRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new FichaTecnicaException("Item com ID " + id + " não encontrado"));

        item.setNome(nome);
        item.setUnidadeMedida(unidadeMedida);
        item.setValor(valor);

        Item itemAtualizado = itemRepositoryPort.salvar(item);
        registrarHistoricoItemPort.registrar(itemAtualizado.getCodigo(), valor, LocalDate.now());

        List<ItemProduto> itemProdutos = itemProdutoRepositoryPort.buscarPorItemId(itemAtualizado.getCodigo());
        for (ItemProduto itemProduto : itemProdutos) {
            ConversaoValoresDTO conversaoValoresDTO = obterValoresConversaoPort.obterValoresConversao(
                    itemProduto.getItem(),
                    itemProduto.getQuantidade(),
                    itemProduto.getUnidadePara().getCodigo()
            );
            itemProduto.setValor(conversaoValoresDTO.valor());
            itemProdutoRepositoryPort.salvar(itemProduto);

            Produto produto = itemProduto.getProduto() != null
                    ? itemProduto.getProduto()
                    : produtoRepositoryPort.buscarPorId(itemProduto.getId().getProdutoId())
                    .orElseThrow(() -> new FichaTecnicaException("Produto não encontrado"));
            QuantidadeValorDTO quantidadeValorDTO = calcularQuantidadeEValorTotal(produto.getCodigo());
            produto.setValorItens(quantidadeValorDTO.valorTotal());
            produtoRepositoryPort.salvar(produto);
        }

        return itemAtualizado;
    }

    private QuantidadeValorDTO calcularQuantidadeEValorTotal(Long produtoId) {
        int quantidadeTotal = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemProduto itemProduto : itemProdutoRepositoryPort.buscarPorProdutoId(produtoId)) {
            quantidadeTotal += 1;
            if (itemProduto.getValor() != null) {
                valorTotal = valorTotal.add(itemProduto.getValor());
            }
        }

        return new QuantidadeValorDTO(quantidadeTotal, valorTotal);
    }

}


