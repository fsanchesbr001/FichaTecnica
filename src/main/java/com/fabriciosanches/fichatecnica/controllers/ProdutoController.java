package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoCompletoDTO;
import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.enums.ImagemPosicao;
import com.fabriciosanches.fichatecnica.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.enums.TipoRelatorio;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.GraficoService;
import com.fabriciosanches.fichatecnica.services.ItensProdutoService;
import com.fabriciosanches.fichatecnica.services.ProdutoService;
import com.fabriciosanches.fichatecnica.services.RelatorioService;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@RestController
@RequestMapping("ficha-tecnica")
@Tag(name = "Produtos", description = "Cadastro, consulta, atualização, exclusão e relatórios de produtos")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoController {
    private static final Logger logger = LogManager.getLogger(ProdutoController.class);

    final ProdutoService produtoService;
    final RelatorioService relatorioService;
    final ItensProdutoService itensProdutoService;
    final GraficoService graficoService;

    @Value("${digitalocean.storage.base-path:/olivander/ficha_tecnica/imagens}")
    private String storagePath;

    @Value("${digitalocean.storage.public-url:http://localhost:8080/uploads}")
    private String publicUrl;

    public ProdutoController(ProdutoService produtoService,
                             RelatorioService relatorioService,
                             ItensProdutoService itensProdutoService,
                             GraficoService graficoService) {
        this.produtoService = produtoService;
        this.relatorioService = relatorioService;
        this.itensProdutoService = itensProdutoService;
        this.graficoService = graficoService;
    }

    @GetMapping("/produtos")
    @Operation(summary = "Lista produtos", description = "Retorna todos os produtos cadastrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhum produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Erro ao consultar produtos")
    })
    public ResponseEntity<List<ProdutoDTO>> buscarLista(){
        logger.info("Inicio do método buscarLista");
        logger.info("Buscando lista de produto");
        try {
            List<ProdutoDTO> produtos = produtoService.listar();
            if (produtos.isEmpty()){
                logger.info("Lista de produtos vazia");
                logger.info("Fim do método buscarLista");
                return ResponseEntity.noContent().build();
            }
            logger.info("Lista de produtos encontrada: {}", produtos);
            logger.info("Fim do método buscarLista");
            return ResponseEntity.ok(produtos);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar lista de produtos", e);
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping("/produtos")
    @Transactional
    @Operation(summary = "Cadastra produto", description = "Cria um novo produto na base de dados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para cadastro")
    })
    public ResponseEntity<ProdutoDTO> cadastrarProduto(@RequestBody ProdutoDTO produto) {
        logger.info("Inicio do método cadastrarProduto");
        logger.info("Cadastrando produto: {}", produto);
        try {
            ProdutoDTO novoProduto = produtoService.cadastrarProduto(produto);
            logger.info("Produto cadastrado com sucesso: {}", novoProduto);
            logger.info("Fim do método cadastrarProduto");
            return ResponseEntity.ok(novoProduto);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao cadastrar produto", e);
            return ResponseEntity.badRequest().build();
        }
    }

     /**
      * Gera um PDF com a lista completa de Produtos.
      * Colunas exibidas: Nome, Descrição, Valor de Venda e Valor dos Itens.
      */
     @PreAuthorize("hasRole('ADMIN')")
     @GetMapping("/produtos/gerar-pdf-lista")
      @Operation(summary = "Gera PDF da lista de produtos", description = "Exporta a lista completa de produtos em PDF.")
      @ApiResponses({
              @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
              @ApiResponse(responseCode = "204", description = "Nenhum produto encontrado para o relatório"),
              @ApiResponse(responseCode = "400", description = "Parâmetros inválidos para geração do PDF"),
              @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o relatório")
      })
     public ResponseEntity<byte[]> gerarPdfLista() {
         logger.info("Início do método gerarPdfLista – ProdutoController");
         try {
             List<ProdutoDTO> lista = produtoService.listar();

             if (lista.isEmpty()) {
                 logger.warn("Nenhum produto encontrado para gerar o relatório");
                 return ResponseEntity.noContent().build();
             }

             String jsonData = new Gson().toJson(lista);

             Map<String, String> colunas = new LinkedHashMap<>();
             colunas.put("nome", "Nome");
             colunas.put("descricao", "Descrição");
             colunas.put("valorVenda", "Valor de Venda");
             colunas.put("valorItens", "Valor dos Itens");

             RelatorioRequestDTO request = new RelatorioRequestDTO(
                     jsonData,
                     "",
                     "Lista de Produtos",
                     colunas,
                     TipoRelatorio.LISTA,
                     OrientacaoRelatorio.RETRATO,
                     true
             );

             byte[] pdfBytes = relatorioService.gerarRelatorioPDF(request);

             String timestamp = LocalDateTime.now()
                     .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
             String filename = "Lista-Produtos-" + timestamp + ".pdf";

             return ResponseEntity.ok()
                     .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                     .contentType(MediaType.APPLICATION_PDF)
                     .body(pdfBytes);

         } catch (IllegalArgumentException e) {
             logger.error("Parâmetros inválidos para geração do PDF de produtos: {}", e.getMessage());
             return ResponseEntity.badRequest().build();
         } catch (Exception e) {
             logger.error("Erro inesperado ao gerar PDF de lista de produtos", e);
             return ResponseEntity.internalServerError().build();
         }
     }

     /**
      * Gera um PDF detalhado para um Produto específico, identificado por {id}.
      * O relatório exibe todos os campos do produto no formato de ficha (DETALHE / PAISAGEM).
      * Se o produto possuir imagem cadastrada, ela é exibida após o título, em escala proporcional.
      */
     @PreAuthorize("hasRole('ADMIN')")
     @GetMapping("/produtos/gerar-pdf-detalhe/{id:[0-9]+}")
      @Operation(summary = "Gera PDF detalhado do produto", description = "Exporta a ficha detalhada de um produto específico em PDF.")
      @ApiResponses({
              @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
              @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
              @ApiResponse(responseCode = "400", description = "Parâmetros inválidos para geração do PDF"),
              @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o relatório")
      })
     public ResponseEntity<byte[]> gerarPdfDetalhe(@PathVariable Long id) {
         logger.info("Início do método gerarPdfDetalhe – ProdutoController – id: {}", id);
         try {
             ProdutoDTO produto = produtoService.buscarPorId(id);
             List<ProdutoCompletoDTO> itensProduto = itensProdutoService.listarItensProduto(id);
              List<Long> codigosUnidade = itensProduto.stream()
                      .map(ProdutoCompletoDTO::cdUnidade)
                      .filter(java.util.Objects::nonNull)
                      .distinct()
                      .toList();
             Map<Long, String> descricoesUnidade = itensProdutoService.obterDescricoesUnidade(codigosUnidade);

             String jsonData = new Gson().toJson(List.of(montarRegistroRelatorio(produto, itensProduto, descricoesUnidade)));

             Map<String, String> colunas = new LinkedHashMap<>();
             colunas.put("nome", "Nome");
             colunas.put("descricao", "Descrição");
             colunas.put("valorVenda", "Valor de Venda");
             colunas.put("valorItens", "Valor dos Itens");

             byte[] imagemProdutoBytes = carregarImagemProduto(produto.imagem(), id);
             byte[] graficoPngBytes = null;

             // Gera gráfico de composição para adicionar no final do relatório.
             try {
                 GraficoPizzaDTO graficoDTO = itensProdutoService.gerarGraficoPizza(id);
                 if (graficoDTO != null && graficoDTO.valores() != null && !graficoDTO.valores().isEmpty()) {
                     graficoPngBytes = graficoService.gerarGraficoPizzaPNG(graficoDTO);
                     logger.info("Gráfico de composição de custo gerado para produto id={}", id);
                 }
             } catch (FichaTecnicaException ex) {
                 logger.info("Sem composição suficiente para gráfico no produto id={} – PDF seguirá sem gráfico", id);
             } catch (Exception ex) {
                 logger.warn("Falha ao gerar gráfico de composição do produto id={}: {}", id, ex.getMessage());
             }

             RelatorioRequestDTO request;
             if (imagemProdutoBytes != null && graficoPngBytes != null) {
                 request = new RelatorioRequestDTO(
                         jsonData,
                         "",
                         "Detalhe do Produto",
                         colunas,
                         TipoRelatorio.DETALHE,
                         OrientacaoRelatorio.PAISAGEM,
                         false,
                         true,
                         imagemProdutoBytes,
                         ImagemPosicao.INICIO,
                         graficoPngBytes,
                         ImagemPosicao.FIM
                 );
             } else if (imagemProdutoBytes != null) {
                 request = new RelatorioRequestDTO(
                         jsonData,
                         "",
                         "Detalhe do Produto",
                         colunas,
                         TipoRelatorio.DETALHE,
                         OrientacaoRelatorio.PAISAGEM,
                         false,
                         true,
                         imagemProdutoBytes,
                         ImagemPosicao.INICIO
                 );
             } else if (graficoPngBytes != null) {
                 request = new RelatorioRequestDTO(
                         jsonData,
                         "",
                         "Detalhe do Produto",
                         colunas,
                         TipoRelatorio.DETALHE,
                         OrientacaoRelatorio.PAISAGEM,
                         false,
                         true,
                         graficoPngBytes,
                         ImagemPosicao.FIM
                 );
             } else {
                 request = new RelatorioRequestDTO(
                         jsonData,
                         "",
                         "Detalhe do Produto",
                         colunas,
                         TipoRelatorio.DETALHE,
                         OrientacaoRelatorio.PAISAGEM,
                         false
                 );
             }

             byte[] pdfBytes = relatorioService.gerarRelatorioPDF(request);

             String timestamp = LocalDateTime.now()
                     .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
             String filename = "Detalhe-Produto-" + id + "-" + timestamp + ".pdf";

             logger.info("PDF de detalhe de Produto gerado com sucesso – arquivo: '{}'", filename);

             return ResponseEntity.ok()
                     .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                     .contentType(MediaType.APPLICATION_PDF)
                     .body(pdfBytes);

         } catch (FichaTecnicaException e) {
             logger.error("Produto não encontrado para id {}: {}", id, e.getMessage());
             return ResponseEntity.notFound().build();
         } catch (IllegalArgumentException e) {
             logger.error("Parâmetros inválidos para geração do PDF de detalhe de Produto: {}", e.getMessage());
             return ResponseEntity.badRequest().build();
         } catch (Exception e) {
             logger.error("Erro inesperado ao gerar PDF de detalhe de Produto", e);
             return ResponseEntity.internalServerError().build();
         }
     }

    private Map<String, Object> montarRegistroRelatorio(ProdutoDTO produto,
                                                        List<ProdutoCompletoDTO> itensProduto,
                                                        Map<Long, String> descricoesUnidade) {
        Map<String, Object> registro = new LinkedHashMap<>();
        registro.put("nome", produto.nome());
        registro.put("descricao", produto.descricao());
        registro.put("valorVenda", produto.valorVenda());
        registro.put("valorItens", produto.valorItens());
        registro.put("itensComposicaoTabelaJson", montarItensComposicaoTabelaJson(itensProduto, descricoesUnidade));
        return registro;
    }

    private String montarItensComposicaoTabelaJson(List<ProdutoCompletoDTO> itensProduto,
                                                   Map<Long, String> descricoesUnidade) {
        if (itensProduto == null || itensProduto.isEmpty()) {
            return "[]";
        }

        Map<Long, String> mapaUnidades = descricoesUnidade != null ? descricoesUnidade : Map.of();
        NumberFormat quantidadeBr = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        quantidadeBr.setMinimumFractionDigits(0);
        quantidadeBr.setMaximumFractionDigits(2);
        NumberFormat moedaBr = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        List<Map<String, String>> linhas = new ArrayList<>();
        for (ProdutoCompletoDTO item : itensProduto) {
            Map<String, String> linha = new LinkedHashMap<>();
            linha.put("item", item.nomeItem() != null ? item.nomeItem() : "Item sem nome");
            linha.put("quantidade", item.qtdeItem() != null ? quantidadeBr.format(item.qtdeItem()) : "0");
            linha.put("medida", item.cdUnidade() != null ? mapaUnidades.getOrDefault(item.cdUnidade(), "Unid " + item.cdUnidade()) : "-");
            linha.put("valor", item.valorItem() != null ? moedaBr.format(item.valorItem()) : moedaBr.format(0));
            linhas.add(linha);
        }

        return new Gson().toJson(linhas);
    }

    private byte[] carregarImagemProduto(String imagemUrl, Long idProduto) {
        if (imagemUrl == null || imagemUrl.isBlank()) {
            return null;
        }
        try {
            String relativePath = imagemUrl.replace(publicUrl, "");
            Path imagePath = Paths.get(storagePath + relativePath).normalize();
            if (Files.exists(imagePath)) {
                logger.info("Imagem do produto id={} carregada: {}", idProduto, imagePath);
                return Files.readAllBytes(imagePath);
            }
            logger.warn("Arquivo de imagem não encontrado para produto id={}: {}", idProduto, imagePath);
        } catch (Exception e) {
            logger.warn("Não foi possível carregar a imagem do produto id={}: {}", idProduto, e.getMessage());
        }
        return null;
    }

    @GetMapping("/produtos/{id:[0-9]+}")
    @Operation(summary = "Busca produto por ID", description = "Retorna os dados de um produto específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "204", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "404", description = "Erro ao buscar produto")
    })
    public ResponseEntity<ProdutoDTO> buscarPorId(@PathVariable Long id){
        logger.info("Inicio do método buscarPorId");
        logger.info("Buscando produto por id: {}", id);
        try {
            ProdutoDTO produto = produtoService.buscarPorId(id);
            if (produto == null){
                logger.info("Produto não encontrado");
                logger.info("Fim do método buscarPorId");
                return ResponseEntity.noContent().build();
            }
            logger.info("Produto encontrado: {}", produto);
            logger.info("Fim do método buscarPorId");
            return ResponseEntity.ok(produto);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar produto por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/produtos/{id:[0-9]+}")
    @Transactional
    @Operation(summary = "Atualiza produto", description = "Altera os dados de um produto existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ProdutoDTO> atualizarProduto(@PathVariable Long id, @RequestBody ProdutoDTO produto) {
        logger.info("Inicio do método atualizarProduto");
        logger.info("Atualizando produto por id: {}", id);
        try {
            ProdutoDTO novoProduto = produtoService.atualizarProduto(id, produto);
            logger.info("Produto atualizado com sucesso: {}", novoProduto);
            logger.info("Fim do método atualizarProduto");
            return ResponseEntity.ok(novoProduto);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao atualizar unidade de medida por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/produtos/{id:[0-9]+}")
    @Transactional
    @Operation(summary = "Remove produto", description = "Exclui um produto existente pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        logger.info("Inicio do método apagar");
        logger.info("Apagando produto por id: {}", id);
        try {
            produtoService.deletarProduto(id);
            logger.info("Produto apagado com sucesso");
            logger.info("Fim do método apagar");
            return ResponseEntity.noContent().build();
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao apagar produto por id", e);
            return ResponseEntity.notFound().build();
        }
    }
 }
