package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemProdutoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoCompletoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutosPorItemDTO;
import com.fabriciosanches.fichatecnica.dtos.QuantidadeValorDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.ItensProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ficha-tecnica")
@Tag(name = "Itens de Produto", description = "Associação entre itens e produtos, valores totais e gráfico de composição")
@SecurityRequirement(name = "bearerAuth")
public class ItemProdutoController {

    private static final Logger logger = LogManager.getLogger(ItemProdutoController.class);

    private final ItensProdutoService itensProdutoService;

    public ItemProdutoController(ItensProdutoService itensProdutoService) {
        this.itensProdutoService = itensProdutoService;
    }

    @PostMapping("/produtos/{idProduto}/itens")
    @Operation(summary = "Salva itens do produto", description = "Associa uma lista de itens a um produto informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itens vinculados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao salvar itens do produto")
    })
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
    @Operation(summary = "Lista itens de um produto", description = "Retorna a composição completa de um produto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Composição retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao buscar itens do produto")
    })
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
    @Operation(summary = "Calcula valores do produto", description = "Retorna a quantidade total e o valor total dos itens do produto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valores calculados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao calcular valores")
    })
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
    @Operation(summary = "Lista produtos por item", description = "Retorna todos os produtos vinculados a um item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produtos retornados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao obter produtos")
    })
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
    @Operation(summary = "Remove item do produto", description = "Desfaz a associação entre um item e um produto.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Associação removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao deletar item do produto")
    })
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
    @Operation(summary = "Atualiza quantidade do item no produto", description = "Altera a quantidade de um item em um produto específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quantidade atualizada com sucesso")
    })
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
    @Operation(summary = "Gera gráfico de pizza", description = "Retorna os dados da composição percentual de custo do produto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gráfico gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Dados insuficientes para gerar o gráfico"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o gráfico")
    })
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
