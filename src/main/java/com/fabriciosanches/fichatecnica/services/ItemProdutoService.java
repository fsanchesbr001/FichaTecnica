package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoQtdeValorDTO;
import com.fabriciosanches.fichatecnica.mappers.ItemProdutoMapper;
import com.fabriciosanches.fichatecnica.repository.ItemProdutoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemProdutoService {
    private final ItemProdutoRepository itemProdutoRepository;

    public ItemProdutoService(ItemProdutoRepository itemProdutoRepository) {
        this.itemProdutoRepository = itemProdutoRepository;
    }

    public ItemProdutoDTO save(ItemProdutoDTO itemProdutoDTO) {
        ItemProduto itemProduto = ItemProdutoMapper.INSTANCE.toEntity(itemProdutoDTO);
        itemProduto = itemProdutoRepository.save(itemProduto);
        return ItemProdutoMapper.INSTANCE.toDTO(itemProduto);
    }

    public List<ItemProdutoDTO> findByCdProduto(Long produtoId) {
        List<ItemProduto> itemProdutos = itemProdutoRepository.findByCdProduto(produtoId);
        return itemProdutos.stream().map(ItemProdutoMapper.INSTANCE::toDTO).collect(Collectors.toList());
    }


    public List<ItemProdutoDTO> listAll() {
        List<ItemProduto> itemProdutos = itemProdutoRepository.findAll();
        return itemProdutos.stream().map(ItemProdutoMapper.INSTANCE::toDTO).collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        itemProdutoRepository.deleteById(id);
    }

    public ItemProdutoDTO findById(Long id) {
        ItemProduto itemProduto = itemProdutoRepository.findById(id).orElseThrow();
        return ItemProdutoMapper.INSTANCE.toDTO(itemProduto);
    }

    public ItemProdutoDTO update(Long id, ItemProdutoDTO itemProdutoDTO) {
        ItemProduto itemProduto = ItemProdutoMapper.INSTANCE.toEntity(itemProdutoDTO);
        itemProduto.setCodigo(id);
        itemProduto = itemProdutoRepository.save(itemProduto);
        return ItemProdutoMapper.INSTANCE.toDTO(itemProduto);
    }

    public ItemProdutoQtdeValorDTO getQtdeItensEValorItens(Long produtoId) {
        List<ItemProdutoDTO> listaItens = findByCdProduto(produtoId);
        Integer quantidadeItens = Long.valueOf(listaItens.size()).intValue();
        double valor = listaItens.stream().mapToDouble(
                ItemProdutoDTO::valor).sum();
        BigDecimal valorTotal = BigDecimal.valueOf(valor);
        return new ItemProdutoQtdeValorDTO(produtoId, quantidadeItens, valorTotal);
    }
}
