package com.fabriciosanches.fichatecnica.controllers;


import com.fabriciosanches.fichatecnica.dtos.ItemProdutoRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoResponseDTO;

import com.fabriciosanches.fichatecnica.services.ItemProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item_produto")
public class ItemProdutoController {

    private final ItemProdutoService itemProdutoService;

    public ItemProdutoController(ItemProdutoService itemProdutoService) {
        this.itemProdutoService = itemProdutoService;
    }

    @PostMapping
    public ResponseEntity<ItemProdutoResponseDTO> adicionarItemProduto(@RequestBody @Valid @NonNull ItemProdutoRequestDTO itemProdutoRequestDTO) {
        ItemProdutoResponseDTO novoItemProduto = itemProdutoService.save(itemProdutoRequestDTO);
        return ResponseEntity.ok(novoItemProduto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemProdutoResponseDTO> alterarItemProduto(@PathVariable Long id, @RequestBody ItemProdutoRequestDTO itemProdutoAtual) {
        ItemProdutoResponseDTO itemProdutoAtualizado = itemProdutoService.update(id, itemProdutoAtual);
        return ResponseEntity.ok(itemProdutoAtualizado);
    }

    @GetMapping
    public ResponseEntity<List<ItemProdutoResponseDTO>> listarItemProdutos() {
        List<ItemProdutoResponseDTO> itemProdutos = itemProdutoService.listAll();
        return ResponseEntity.ok(itemProdutos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirItemProduto(@PathVariable Long id) {
        ItemProdutoResponseDTO itemProdutoResponseDTO = itemProdutoService.findById(id);
        if (itemProdutoResponseDTO == null) {
            return ResponseEntity.notFound().build();
        }
        itemProdutoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<ItemProdutoResponseDTO>> listarItensPorProduto(@PathVariable Long produtoId) {
        List<ItemProdutoResponseDTO> itens = itemProdutoService.findByCdProduto(produtoId);
        return ResponseEntity.ok(itens);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<ItemProdutoResponseDTO>> listarProdutosPorItem(@PathVariable Long itemId) {
        List<ItemProdutoResponseDTO> produtos = itemProdutoService.findByCdItem(itemId);
        return ResponseEntity.ok(produtos);
    }
}

