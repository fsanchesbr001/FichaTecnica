package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.ItemProdutoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoCompleto;
import com.fabriciosanches.fichatecnica.services.ItensProdutoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ficha-tecnica")
public class ItemProdutoController {

    private static final Logger logger = LogManager.getLogger(ItemProdutoController.class);

    private final ItensProdutoService itensProdutoService;

    public ItemProdutoController(ItensProdutoService itensProdutoService) {
        this.itensProdutoService = itensProdutoService;
    }


    @PostMapping("/produtos/{idProduto}/itens")
    public ResponseEntity<List<ProdutoCompleto>> salvarItemProduto(@PathVariable("idProduto") Long idProduto,
                                                             @RequestBody List<ItemProdutoDTO> itemProduto) {
        logger.info("Inicio do método salvarItemProduto");
        logger.info("Salvando item produto");
        try {
            List<ProdutoCompleto> produtoCompletoList = itensProdutoService.salvar(idProduto, itemProduto);
            logger.info("Item produto salvo");
            logger.info("Fim do método salvarItemProduto");
            return ResponseEntity.ok(produtoCompletoList);
        } catch (Exception e) {
            logger.error("Erro ao salvar item produto", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
