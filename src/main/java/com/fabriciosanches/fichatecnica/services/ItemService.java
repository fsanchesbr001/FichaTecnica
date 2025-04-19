package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.HistoricoItem;
import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.domains.Produto;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemDTO;
import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.dtos.QuantidadeValorDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.HistoricoItemRepository;
import com.fabriciosanches.fichatecnica.repository.ItemProdutoRepository;
import com.fabriciosanches.fichatecnica.repository.ItemRepository;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ItemService {

    private final Logger logger = LogManager.getLogger(ItemService.class);
    private final ItemRepository repository;
    private final HistoricoItemRepository historicoItemRepository;
    private final ItemProdutoRepository itemProdutoRepository;
    private final ConversaoService conversaoService;
    private final ProdutoRepository produtoRepository;


    public ItemService(ItemRepository repository,
                       HistoricoItemRepository historicoItemRepository,
                       ItemProdutoRepository itemProdutoRepository, ConversaoService conversaoService, ProdutoRepository produtoRepository) {
        this.repository = repository;
        this.historicoItemRepository = historicoItemRepository;
        this.itemProdutoRepository = itemProdutoRepository;
        this.conversaoService = conversaoService;
        this.produtoRepository = produtoRepository;
    }

    private Optional<List<ItemDTO>> obterLista() {
        logger.info("Inicio do método obterLista");
        Optional<List<ItemDTO>> listaRecord =
                Optional.of(ItemDTO.from(repository.findAll()));

        logger.info("Lista de itens encontrada: {}", listaRecord.get());
        logger.info("Fim do método obterLista");
        return listaRecord;
    }

    public List<ItemDTO> listar() {
        return obterLista().map(lista -> lista.stream()
                .sorted(Comparator.comparing(ItemDTO::nome))
                .toList()).orElseThrow(
                () -> new FichaTecnicaException("Lista de itens não encontrada"));

    }

    public ItemDTO buscarPorId(Long id) {
        return repository.findAll().stream()
                .map(ItemDTO::new)
                .filter(item -> id.equals(item.codigo()))
                .findFirst()
                .orElseThrow(() -> new FichaTecnicaException("Item com ID " + id + " não encontrado"));
    }

    public long findByName(String nome) {
        return repository.countByName(nome);
    }

    @Modifying
    @Transactional
    public ItemDTO atualizarItem(Long id, ItemDTO novosDados) {
        // Buscar o item existente
        Optional<Item> itemExistente = repository.findById(id);
        if (itemExistente.isEmpty()) {
            throw new FichaTecnicaException("Item com ID " + id + " não encontrado");
        }

        Item item = itemExistente.get();
        item.setNome(novosDados.nome());
        item.setValor(novosDados.valor());
        item.setUnidadeMedida(novosDados.unidadeMedida());

        // Atualize outros campos conforme necessário
        ItemDTO itemDTO = new ItemDTO(repository.save(item));

        // Salvar o histórico
        HistoricoItem historicoItem = new HistoricoItem(null,itemDTO.codigo() , novosDados.valor(), LocalDate.now());
        historicoItemRepository.save(historicoItem);

        // Verificar se o item está associado a algum produto
        List<ItemProduto> itemProdutos = itemProdutoRepository.findByItem(item);

        if (!itemProdutos.isEmpty()) {
            // Atualizar o valor do item nos produtos associados
            for (ItemProduto itemProduto : itemProdutos) {
               // Obter a conversão de valores
                ConversaoValoresDTO conversaoValoresDTO = conversaoService.
                        obterValoresConversao(
                        itemProduto.getItem(),
                        itemProduto.getQuantidade(),
                        itemProduto.getUnidadePara().getCodigo()
                );
                itemProduto.setValor(conversaoValoresDTO.valor());
                itemProdutoRepository.save(itemProduto);

                // Atualizar o valor do produto associado
                Produto produto = itemProduto.getProduto();

                QuantidadeValorDTO quantidadeValorDTO = calcularQuantidadeEValorTotal(produto);
                produto.setValorItens(quantidadeValorDTO.valorTotal());
                produtoRepository.save(produto);
            }
        }

        // Retornar o DTO atualizado
        return itemDTO;
    }

    @Modifying
    @Transactional
    public ItemDTO cadastrarItem(ItemDTO item) {
        Objects.requireNonNull(item, "Item não pode ser nulo");
        Objects.requireNonNull(item.nome(), "Nome do item não pode ser nulo");
        Objects.requireNonNull(item.unidadeMedida(), "Unidade de medida não pode ser nula");
        Objects.requireNonNull(item.valor(), "Valor do item não pode ser nulo");

        if (findByName(item.nome()) > 0) {
            throw new FichaTecnicaException("Item já cadastrado");
        }

        Item novoItem = new Item(item);

        ItemDTO itemDTO = new ItemDTO(repository.save(novoItem));

        HistoricoItem historicoItem = new HistoricoItem(null, itemDTO.codigo(), item.valor(), LocalDate.now());

        historicoItemRepository.save(historicoItem);

        return itemDTO;
    }

    @Modifying
    @Transactional
    public void deletarItem(Long id) {

        logger.info("Inicio do método deletarItem");
        Item item = repository.findById(id)
                .orElseThrow(() -> new FichaTecnicaException("Item com ID " + id + " não encontrado"));
        logger.info("Item encontrado: {}", item);

        List<HistoricoItem> historicoItemList = historicoItemRepository.findByCdItem(id);

        for (HistoricoItem historicoItem : historicoItemList) {
            historicoItemRepository.deleteHistoricoItemByCdItem(historicoItem.getCdItem());
        }

        List<ItemProduto> itemProdutoList = itemProdutoRepository.findByItem(item);
        for (ItemProduto itemProduto : itemProdutoList) {
            Produto produto = itemProduto.getProduto();
            //QuantidadeValorDTO quantidadeValorDTO = calcularQuantidadeEValorTotal(produto);
            produto.setValorItens(produto.getValorItens().subtract(itemProduto.getValor()));
            produtoRepository.save(produto);
            itemProdutoRepository.deleteItemProduto(itemProduto.getId().getItemId());
        }
        repository.deleteItem(item.getCodigo());
    }

    public QuantidadeValorDTO calcularQuantidadeEValorTotal(Produto produto) {
        logger.info("Inicio do método calcularQuantidadeEValorTotal");

        int quantidadeTotal = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemProduto itemProduto : produto.getProdutosList()) {
            quantidadeTotal += 1;
            valorTotal = valorTotal.add(itemProduto.getValor());
        }

        return new QuantidadeValorDTO(quantidadeTotal, valorTotal);
    }
}
