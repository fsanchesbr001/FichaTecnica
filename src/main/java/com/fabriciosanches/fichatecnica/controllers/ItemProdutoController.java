package com.fabriciosanches.fichatecnica.controllers;


import com.fabriciosanches.fichatecnica.dtos.ItemProdutoDTO;

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
    public ResponseEntity<ItemProdutoDTO> adicionarItemProduto(@RequestBody @Valid @NonNull ItemProdutoDTO itemProdutoDTO) {
        ItemProdutoDTO novoItemProduto = itemProdutoService.save(itemProdutoDTO);
        return ResponseEntity.ok(novoItemProduto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemProdutoDTO> alterarItemProduto(@PathVariable Long id, @RequestBody ItemProdutoDTO itemProdutoAtual) {
        ItemProdutoDTO itemProdutoAtualizado = itemProdutoService.update(id, itemProdutoAtual);
        return ResponseEntity.ok(itemProdutoAtualizado);
    }

    @GetMapping
    public ResponseEntity<List<ItemProdutoDTO>> listarItemProdutos() {
        List<ItemProdutoDTO> itemProdutos = itemProdutoService.listAll();
        return ResponseEntity.ok(itemProdutos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirItemProduto(@PathVariable Long id) {
        ItemProdutoDTO itemProdutoResponseDTO = itemProdutoService.findById(id);
        if (itemProdutoResponseDTO == null) {
            return ResponseEntity.notFound().build();
        }
        itemProdutoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<ItemProdutoDTO>> listarItensPorProduto(@PathVariable Long produtoId) {
        List<ItemProdutoDTO> itens = itemProdutoService.findByCdProduto(produtoId);
        return ResponseEntity.ok(itens);
    }
}

