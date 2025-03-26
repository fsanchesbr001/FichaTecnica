package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.*;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoCompletoDTO;
import com.fabriciosanches.fichatecnica.dtos.QuantidadeValorDTO;
import com.fabriciosanches.fichatecnica.dtos.UnidadeMedidaDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ItemProdutoRepository;
import com.fabriciosanches.fichatecnica.repository.ItemRepository;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItensProdutoService {

    private final Logger logger = LogManager.getLogger(ItensProdutoService.class);

    private final ItemProdutoRepository itemProdutoRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemRepository itemRepository;
    private final UnidadeMedidaService unidadeMedidaService;


    public ItensProdutoService(ItemProdutoRepository itemProdutoRepository,
                               ProdutoRepository produtoRepository,
                               ItemRepository itemRepository,
                               UnidadeMedidaService unidadeMedidaService) {
        this.itemProdutoRepository = itemProdutoRepository;
        this.produtoRepository = produtoRepository;
        this.itemRepository = itemRepository;
        this.unidadeMedidaService = unidadeMedidaService;
    }

    public ItemProduto buscarPorId(Long id) {
        return itemProdutoRepository.findById(id).orElse(null);
    }

    public List<ProdutoCompletoDTO> salvar(Long idProduto, List<ItemProdutoDTO> itemProduto) {
        logger.info("Inicio do método salvar com Lista");
        Produto produto = getProduto(idProduto);

        if(!isValidItens(itemProduto)){
            logger.error("Item não encontrado");
            throw new FichaTecnicaException("Item não encontrado");
        }

        var produtoCompletoList = new ArrayList<ProdutoCompletoDTO>();
        for (var item: itemProduto) {
            var itemEntity = getItem(item);
            var unidadeMedida = getUnidadeMedidaDTO(item);
            saveItem(item, produto, itemEntity, unidadeMedida);
            produtoCompletoList.add(new ProdutoCompletoDTO(produto.getNome(),itemEntity.getNome(),
                    item.qtdItem(), item.cdUnidadeMedida(),
                    item.vlrItem()));
        }

        return produtoCompletoList;
    }

    public List<ProdutoCompletoDTO> listarItensProduto(Long idProduto) {
        logger.info("Inicio do método listarItensProduto");
        Produto produto = getProduto(idProduto);

        return produto.getProdutosList().stream().map(ip-> new ProdutoCompletoDTO(
                ip.getProduto().getNome(),
                ip.getItem().getNome(),ip.getQuantidade(),
                ip.getUnidadePara().getCodigo(),ip.getValor())
        ).collect(Collectors.toList());
    }

    public QuantidadeValorDTO calcularQuantidadeEValorTotal(Long idProduto) {
        logger.info("Inicio do método calcularQuantidadeEValorTotal");
        Produto produto = getProduto(idProduto);

        int quantidadeTotal = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemProduto itemProduto : produto.getProdutosList()) {
            quantidadeTotal += 1;
            valorTotal = valorTotal.add(itemProduto.getValor());
        }

        return new QuantidadeValorDTO(quantidadeTotal, valorTotal);
    }
    private Boolean isValidItens(List<ItemProdutoDTO> itemProduto) {
        var listItem = itemRepository.findAll();
        var itemIdsCompleta = listItem.stream().map(Item::getCodigo)
                .collect(Collectors.toSet());

        return  itemProduto.stream().allMatch(item -> itemIdsCompleta.contains(item.cdItem()));
    }

    private void saveItem(ItemProdutoDTO itemProduto, Produto produto, Item item, UnidadeMedidaDTO unidadeMedida) {
        var itemProdutoSalvo = new ItemProduto();
        itemProdutoSalvo.setId(new ItemProdutoId(produto.getCodigo(), item.getCodigo()));
        itemProdutoSalvo.setProduto(produto);
        itemProdutoSalvo.setItem(item);
        itemProdutoSalvo.setQuantidade(itemProduto.qtdItem());
        itemProdutoSalvo.setUnidadePara(UnidadeMedidaDTO.toEntity(unidadeMedida));
        itemProdutoSalvo.setValor(itemProduto.vlrItem());
        itemProdutoRepository.save(itemProdutoSalvo);
    }

    private Produto getProduto(Long idProduto) {
        return produtoRepository.findById(idProduto)
                .orElseThrow( () -> {
                    logger.error("Produto não encontrado");
                    return new FichaTecnicaException("Produto não encontrado");
                });
    }

    private UnidadeMedidaDTO getUnidadeMedidaDTO(ItemProdutoDTO itemProduto) {
        UnidadeMedidaDTO unidadeMedida = unidadeMedidaService.buscarPorId(itemProduto.cdUnidadeMedida());
        if (unidadeMedida == null) {
            logger.error("Unidade de medida não encontrada");
            throw new FichaTecnicaException("Unidade de medida não encontrada");
        }
        return unidadeMedida;
    }

    private Item getItem(ItemProdutoDTO itemProduto) {
        return itemRepository.findById(itemProduto.cdItem())
                .orElseThrow();
    }
}
