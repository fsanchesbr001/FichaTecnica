package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.ItemProdutoId;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.AdicionarItemAoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarQuantidadeItemDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CalcularValoresItensProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPizzaProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarItensDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarProdutosPorItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RemoverItemDoProdutoPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ItemProdutoDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutoCompletoDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutosPorItemDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.QuantidadeValorDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("ficha-tecnica")
@Tag(name = "Itens de Produto", description = "AssociaÃ§Ã£o entre itens e produtos, valores totais e grÃ¡fico de composiÃ§Ã£o")
@SecurityRequirement(name = "bearerAuth")
public class ItemProdutoController {

    private static final Logger logger = LogManager.getLogger(ItemProdutoController.class);

    private final AdicionarItemAoProdutoPort adicionarItemAoProdutoPort;
    private final ListarItensDoProdutoPort listarItensDoProdutoPort;
    private final CalcularValoresItensProdutoPort calcularValoresItensProdutoPort;
    private final ListarProdutosPorItemPort listarProdutosPorItemPort;
    private final RemoverItemDoProdutoPort removerItemDoProdutoPort;
    private final AtualizarQuantidadeItemDoProdutoPort atualizarQuantidadeItemDoProdutoPort;
    private final GerarGraficoPizzaProdutoPort gerarGraficoPizzaProdutoPort;

    public ItemProdutoController(
            AdicionarItemAoProdutoPort adicionarItemAoProdutoPort,
            ListarItensDoProdutoPort listarItensDoProdutoPort,
            CalcularValoresItensProdutoPort calcularValoresItensProdutoPort,
            ListarProdutosPorItemPort listarProdutosPorItemPort,
            RemoverItemDoProdutoPort removerItemDoProdutoPort,
            AtualizarQuantidadeItemDoProdutoPort atualizarQuantidadeItemDoProdutoPort,
            GerarGraficoPizzaProdutoPort gerarGraficoPizzaProdutoPort) {
        this.adicionarItemAoProdutoPort = adicionarItemAoProdutoPort;
        this.listarItensDoProdutoPort = listarItensDoProdutoPort;
        this.calcularValoresItensProdutoPort = calcularValoresItensProdutoPort;
        this.listarProdutosPorItemPort = listarProdutosPorItemPort;
        this.removerItemDoProdutoPort = removerItemDoProdutoPort;
        this.atualizarQuantidadeItemDoProdutoPort = atualizarQuantidadeItemDoProdutoPort;
        this.gerarGraficoPizzaProdutoPort = gerarGraficoPizzaProdutoPort;
    }

    @PostMapping("/produtos/{idProduto}/itens")
    @Operation(summary = "Salva itens do produto", description = "Associa uma lista de itens a um produto informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itens vinculados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao salvar itens do produto")
    })
    public ResponseEntity<List<ProdutoCompletoDTO>> salvarItemProduto(@PathVariable("idProduto") Long idProduto,
                                                                      @RequestBody List<ItemProdutoDTO> itemProduto) {
        logger.info("Inicio do mÃ©todo salvarItemProduto");
        try {
            List<ItemProduto> itensDominio = itemProduto.stream().map(dto -> toDomain(idProduto, dto)).toList();
            List<ProdutoCompletoDTO> produtoCompletoList = adicionarItemAoProdutoPort.adicionar(idProduto, itensDominio)
                    .stream()
                    .map(this::toDto)
                    .toList();
            return ResponseEntity.ok(produtoCompletoList);
        } catch (Exception e) {
            logger.error("Erro ao salvar item produto", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/produtos/{idProduto}/itens")
    @Operation(summary = "Lista itens de um produto", description = "Retorna a composiÃ§Ã£o completa de um produto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ComposiÃ§Ã£o retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao buscar itens do produto")
    })
    public ResponseEntity<List<ProdutoCompletoDTO>> buscarItensProduto(@PathVariable("idProduto") Long idProduto) {
        logger.info("Inicio do mÃ©todo buscarItensProduto");
        try {
            List<ProdutoCompletoDTO> produtoCompletoList = listarItensDoProdutoPort.listar(idProduto).stream()
                    .map(this::toDto)
                    .toList();
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
        logger.info("Inicio do mÃ©todo obterValoresItens");
        try {
            QuantidadeValorDTO valores = calcularValoresItensProdutoPort.calcular(idProduto);
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
        logger.info("Inicio do mÃ©todo ListarProdutosPorItem");
        try {
            List<ProdutosPorItemDTO> produtos = listarProdutosPorItemPort.listarPorItem(idItem);
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            logger.error("Erro ao obter produtos", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/produtos/{idProduto}/itens/{idItem}")
    @Operation(summary = "Remove item do produto", description = "Desfaz a associaÃ§Ã£o entre um item e um produto.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "AssociaÃ§Ã£o removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao deletar item do produto")
    })
    public ResponseEntity<Void> deletarItemProduto(@PathVariable("idProduto") Long idProduto,
                                                    @PathVariable("idItem") Long idItem) {
        logger.info("Inicio do mÃ©todo deletarItemProduto");
        try {
            removerItemDoProdutoPort.remover(idProduto, idItem);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erro ao deletar item produto", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{idProduto}/{idItem}/quantidade")
    @Operation(summary = "Atualiza quantidade do item no produto", description = "Altera a quantidade de um item em um produto especÃ­fico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quantidade atualizada com sucesso")
    })
    public ResponseEntity<Void> atualizarQuantidadeItemProduto(
            @PathVariable Long idProduto,
            @PathVariable Long idItem,
            @RequestParam Double novaQuantidade) {
        atualizarQuantidadeItemDoProdutoPort.atualizarQuantidade(idProduto, idItem, novaQuantidade);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/produtos/{idProduto}/grafico-pizza")
    @Operation(summary = "Gera grÃ¡fico de pizza", description = "Retorna os dados da composiÃ§Ã£o percentual de custo do produto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "GrÃ¡fico gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Dados insuficientes para gerar o grÃ¡fico"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o grÃ¡fico")
    })
    public ResponseEntity<GraficoPizzaDTO> gerarGraficoPizza(@PathVariable Long idProduto) {
        logger.info("InÃ­cio do mÃ©todo gerarGraficoPizza â€“ idProduto={}", idProduto);
        try {
            GraficoPizzaDTO grafico = gerarGraficoPizzaProdutoPort.gerar(idProduto);
            logger.info("GrÃ¡fico de pizza gerado com sucesso para idProduto={}", idProduto);
            return ResponseEntity.ok(grafico);
        } catch (FichaTecnicaException e) {
            logger.warn("Dados insuficientes para gerar grÃ¡fico de pizza â€“ idProduto={}: {}", idProduto, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar grÃ¡fico de pizza para idProduto={}", idProduto, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private ItemProduto toDomain(Long idProduto, ItemProdutoDTO dto) {
        return new ItemProduto(
                new ItemProdutoId(idProduto, dto.cdItem()),
                new Item(
                        dto.cdItem(),
                        null,
                        new UnidadeMedida(dto.cdUnidadeMedida(), "TEMP", "TEMP"),
                        null),
                new Produto(idProduto, null, null, null, null, null, List.of()),
                new UnidadeMedida(dto.cdUnidadeMedida(), "TEMP", "TEMP"),
                dto.qtdItem(),
                dto.vlrItem());
    }

    private ProdutoCompletoDTO toDto(ItemProduto itemProduto) {
        return new ProdutoCompletoDTO(
                itemProduto.getProduto() != null ? itemProduto.getProduto().getNome() : null,
                itemProduto.getItem() != null ? itemProduto.getItem().getNome() : null,
                itemProduto.getItem() != null ? itemProduto.getItem().getCodigo() : null,
                itemProduto.getQuantidade(),
                itemProduto.getUnidadePara() != null ? itemProduto.getUnidadePara().getCodigo() : null,
                itemProduto.getValor());
    }
}


