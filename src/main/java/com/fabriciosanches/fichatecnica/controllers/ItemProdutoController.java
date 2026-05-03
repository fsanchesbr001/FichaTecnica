package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoCompletoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutosPorItemDTO;
import com.fabriciosanches.fichatecnica.dtos.QuantidadeValorDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
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
        try {
            List<ProdutoCompletoDTO> produtoCompletoList = itensProdutoService.salvar(idProduto, itemProduto);
            logger.info("Item produto salvo");
            return ResponseEntity.ok(produtoCompletoList);
        } catch (Exception e) {
            logger.error("Erro ao salvar item produto", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/produtos/{idProduto}/itens")
    public ResponseEntity<List<ProdutoCompletoDTO>> buscarItensProduto(@PathVariable("idProduto") Long idProduto) {
        logger.info("Inicio do método buscarItensProduto");
        try {
            List<ProdutoCompletoDTO> produtoCompletoList = itensProdutoService.listarItensProduto(idProduto);
            logger.info("Item produto encontrado");
            return ResponseEntity.ok(produtoCompletoList);
        } catch (Exception e) {
            logger.error("Erro ao buscar item produto", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/produtos/{idProduto}/valores")
    public ResponseEntity<QuantidadeValorDTO> obterValoresItens(@PathVariable("idProduto") Long idProduto) {
        logger.info("Inicio do método obterValoresItens");
        try {
            QuantidadeValorDTO valores = itensProdutoService.calcularQuantidadeEValorTotal(idProduto);
            logger.info("Valores obtidos");
            return ResponseEntity.ok(valores);
        } catch (Exception e) {
            logger.error("Erro ao calcular valores", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/itens/{idItem}/produtos")
    public ResponseEntity<List<ProdutosPorItemDTO>> ListarProdutosPorItem(@PathVariable("idItem") Long idItem) {
        logger.info("Inicio do método ListarProdutosPorItem");
        try {
            List<ProdutosPorItemDTO> produtos = itensProdutoService.listarProdutosPorItem(idItem);
            logger.info("Produtos obtidos");
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
        try {
            itensProdutoService.deletarItemProduto(idProduto, idItem);
            logger.info("Item produto deletado");
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

    /**
     * Retorna os dados para geração de um gráfico de pizza com a composição
     * percentual de custo de cada item do produto informado.
     * <p>
     * Cada fatia contém: nome do item, porcentagem, valor do item,
     * valor total do produto e cor hexadecimal para o Chart.js.
     * </p>
     *
     * @param idProduto código do Produto
     * @return {@link GraficoPizzaDTO} pronto para consumo pelo Angular (ng2-charts)
     */
    @GetMapping("/produtos/{idProduto}/grafico-pizza")
    public ResponseEntity<GraficoPizzaDTO> gerarGraficoPizza(@PathVariable Long idProduto) {
        logger.info("Início do método gerarGraficoPizza – idProduto={}", idProduto);
        try {
            GraficoPizzaDTO grafico = itensProdutoService.gerarGraficoPizza(idProduto);
            logger.info("Gráfico de pizza gerado com sucesso para idProduto={}", idProduto);
            return ResponseEntity.ok(grafico);
        } catch (FichaTecnicaException e) {
            logger.warn("Dados insuficientes para gerar gráfico de pizza – idProduto={}: {}", idProduto, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar gráfico de pizza para idProduto={}", idProduto, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
