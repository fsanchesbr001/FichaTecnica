package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RegistrarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.domains.Produto;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.dtos.QuantidadeValorDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.UnidadeMedidaEntity;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.ItemEntity;
import com.fabriciosanches.fichatecnica.repository.ItemProdutoRepository;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class AtualizarItemUseCase implements AtualizarItemPort {
    private final ItemRepositoryPort itemRepositoryPort;
    private final RegistrarHistoricoItemPort registrarHistoricoItemPort;
    private final ItemProdutoRepository itemProdutoRepository;
    private final ObterValoresConversaoPort obterValoresConversaoPort;
    private final ProdutoRepository produtoRepository;

    public AtualizarItemUseCase(
            ItemRepositoryPort itemRepositoryPort,
            RegistrarHistoricoItemPort registrarHistoricoItemPort,
            ItemProdutoRepository itemProdutoRepository,
            ObterValoresConversaoPort obterValoresConversaoPort,
            ProdutoRepository produtoRepository) {
        this.itemRepositoryPort = Objects.requireNonNull(itemRepositoryPort, "Item repository port não pode ser nulo");
        this.registrarHistoricoItemPort = Objects.requireNonNull(registrarHistoricoItemPort, "Port de histórico não pode ser nulo");
        this.itemProdutoRepository = Objects.requireNonNull(itemProdutoRepository, "ItemProdutoRepository não pode ser nulo");
        this.obterValoresConversaoPort = Objects.requireNonNull(obterValoresConversaoPort, "ObterValoresConversaoPort não pode ser nulo");
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository não pode ser nulo");
    }

    @Override
    public Item atualizar(Long id, String nome, UnidadeMedidaEntity unidadeMedida, BigDecimal valor) {
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

        List<ItemProduto> itemProdutos = itemProdutoRepository.findByItemCodigo(itemAtualizado.getCodigo());
        for (ItemProduto itemProduto : itemProdutos) {
            ConversaoValoresDTO conversaoValoresDTO = obterValoresConversaoPort.obterValoresConversao(
                    toDomain(itemProduto.getItem()),
                    itemProduto.getQuantidade(),
                    itemProduto.getUnidadePara().getCodigo()
            );
            itemProduto.setValor(conversaoValoresDTO.valor());
            itemProdutoRepository.save(itemProduto);

            Produto produto = itemProduto.getProduto();
            QuantidadeValorDTO quantidadeValorDTO = calcularQuantidadeEValorTotal(produto);
            produto.setValorItens(quantidadeValorDTO.valorTotal());
            produtoRepository.save(produto);
        }

        return itemAtualizado;
    }

    private QuantidadeValorDTO calcularQuantidadeEValorTotal(Produto produto) {
        int quantidadeTotal = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemProduto itemProduto : produto.getProdutosList()) {
            quantidadeTotal += 1;
            valorTotal = valorTotal.add(itemProduto.getValor());
        }

        return new QuantidadeValorDTO(quantidadeTotal, valorTotal);
    }

    private Item toDomain(ItemEntity entity) {
        return new Item(entity.getCodigo(), entity.getNome(), entity.getUnidadeMedida(), entity.getValor());
    }
}

