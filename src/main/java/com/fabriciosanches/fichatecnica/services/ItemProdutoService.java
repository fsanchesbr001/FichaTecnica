package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.ItemProduto;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoQtdeValorDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoResponseDTO;
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

    public ItemProdutoResponseDTO save(ItemProdutoRequestDTO itemProdutoRequestDTO) {
        ItemProduto itemProduto = ItemProdutoMapper.toEntity(itemProdutoRequestDTO);
        itemProduto = itemProdutoRepository.save(itemProduto);
        return ItemProdutoMapper.toResponseDTO(itemProduto);
    }

    public List<ItemProdutoResponseDTO> findByCdProduto(Long produtoId) {
        List<ItemProduto> itemProdutos = itemProdutoRepository.findByCdProduto(produtoId);
        return itemProdutos.stream().map(ItemProdutoMapper::toResponseDTO).collect(Collectors.toList());
    }

    public List<ItemProdutoResponseDTO> findByCdItem(Long itemId) {
        List<ItemProduto> itemProdutos = itemProdutoRepository.findByCdItem(itemId);
        return itemProdutos.stream().map(ItemProdutoMapper::toResponseDTO).collect(Collectors.toList());
    }

    public List<ItemProdutoResponseDTO> listAll() {
        List<ItemProduto> itemProdutos = itemProdutoRepository.findAll();
        return itemProdutos.stream().map(ItemProdutoMapper::toResponseDTO).collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        itemProdutoRepository.deleteById(id);
    }

    public ItemProdutoResponseDTO findById(Long id) {
        ItemProduto itemProduto = itemProdutoRepository.findById(id).orElseThrow();
        return ItemProdutoMapper.toResponseDTO(itemProduto);
    }

    public ItemProdutoResponseDTO update(Long id, ItemProdutoRequestDTO itemProdutoRequestDTO) {
        ItemProduto itemProduto = ItemProdutoMapper.toEntity(itemProdutoRequestDTO);
        itemProduto.setCodigo(id);
        itemProduto = itemProdutoRepository.save(itemProduto);
        return ItemProdutoMapper.toResponseDTO(itemProduto);
    }

    public ItemProdutoQtdeValorDTO getQtdeItensEValorItens(Long produtoId) {
        List<ItemProdutoResponseDTO> listaItens = findByCdProduto(produtoId);
        Integer quantidadeItens = Long.valueOf(listaItens.size()).intValue();
        double valor = listaItens.stream().mapToDouble(
                itemProdutoResponseDTO -> itemProdutoResponseDTO.valor().doubleValue()).sum();
        BigDecimal valorTotal = BigDecimal.valueOf(valor);
        return new ItemProdutoQtdeValorDTO(produtoId, quantidadeItens, valorTotal);
    }
}
