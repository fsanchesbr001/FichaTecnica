package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.ItemProdutoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoCompletoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutosPorItemDTO;
import com.fabriciosanches.fichatecnica.dtos.QuantidadeValorDTO;
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
    public ResponseEntity<List<ProdutoCompletoDTO>> salvarItemProduto(@PathVariable("idProduto") Long idProduto,
                                                                      @RequestBody List<ItemProdutoDTO> itemProduto) {
        logger.info("Inicio do método salvarItemProduto");
        logger.info("Salvando item produto");
        try {
            List<ProdutoCompletoDTO> produtoCompletoList = itensProdutoService.salvar(idProduto, itemProduto);
            logger.info("Item produto salvo");
            logger.info("Fim do método salvarItemProduto");
            return ResponseEntity.ok(produtoCompletoList);
        } catch (Exception e) {
            logger.error("Erro ao salvar item produto", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/produtos/{idProduto}/itens")
    public ResponseEntity<List<ProdutoCompletoDTO>> buscarItensProduto(@PathVariable("idProduto") Long idProduto) {
        logger.info("Inicio do método buscarItensProduto");
        logger.info("Buscando item produto");
        try {
            List<ProdutoCompletoDTO> produtoCompletoList = itensProdutoService.listarItensProduto(idProduto);
            logger.info("Item produto encontrado");
            logger.info("Fim do método buscarItensProduto");
            return ResponseEntity.ok(produtoCompletoList);
        } catch (Exception e) {
            logger.error("Erro ao buscar item produto", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/produtos/{idProduto}/valores")
    public ResponseEntity<QuantidadeValorDTO> obterValoresItens(@PathVariable("idProduto") Long idProduto) {
        logger.info("Inicio do método obterValoresItens");
        logger.info("Buscando valores");
        try {
            QuantidadeValorDTO valores = itensProdutoService.calcularQuantidadeEValorTotal(idProduto);
            logger.info("Valores obtidos");
            logger.info("Fim do método obterValoresItens");
            return ResponseEntity.ok(valores);
        } catch (Exception e) {
            logger.error("Erro ao calcular valores", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/itens/{idItem}/produtos")
    public ResponseEntity<List<ProdutosPorItemDTO>> ListarProdutosPorItem(@PathVariable("idItem") Long idItem) {
        logger.info("Inicio do método ListarProdutosPorItem");
        logger.info("Buscando Item");
        try {
            List<ProdutosPorItemDTO> produtos = itensProdutoService.listarProdutosPorItem(idItem);
            logger.info("Produtos obtidos");
            logger.info("Fim do método ListarProdutosPorItem");
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            logger.error("Erro ao obter produtos", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/produtos/{idProduto}/itens/{idItem}")
    public ResponseEntity<Void> deletarItemProduto(@PathVariable("idProduto") Long idProduto,
                                                    @PathVariable("idItem") Long idItem) {
        logger.info("Inicio do método deletarItemProduto");
        logger.info("Deletando item produto");
        try {
            itensProdutoService.deletarItemProduto(idProduto, idItem);
            logger.info("Item produto deletado");
            logger.info("Fim do método deletarItemProduto");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erro ao deletar item produto", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{idProduto}/{idItem}/quantidade")
    public ResponseEntity<Void> atualizarQuantidadeItemProduto(
            @PathVariable Long idProduto,
            @PathVariable Long idItem,
            @RequestParam Double novaQuantidade) {
        itensProdutoService.atualizarQuantidadeItemProduto(idProduto, idItem, novaQuantidade);
        return ResponseEntity.ok().build();
    }
}
