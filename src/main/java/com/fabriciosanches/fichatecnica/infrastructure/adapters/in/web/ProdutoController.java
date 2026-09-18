package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.enums.ImagemPosicao;
import com.fabriciosanches.fichatecnica.core.domain.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.core.domain.enums.TipoRelatorio;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPizzaProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarItensDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterDescricoesUnidadePort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutoCompletoDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutoDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.infrastructure.util.TextoEncodingUtils;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Tag(name = "Produtos", description = "Cadastro, consulta, atualizacao, exclusao e relatorios de produtos")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoController {
    private static final Logger logger = LogManager.getLogger(ProdutoController.class);

    private final BuscarProdutoPort buscarProdutoPort;
    private final CriarProdutoPort criarProdutoPort;
    private final AtualizarProdutoPort atualizarProdutoPort;
    private final DeletarProdutoPort deletarProdutoPort;
    private final ListarItensDoProdutoPort listarItensDoProdutoPort;
    private final ObterDescricoesUnidadePort obterDescricoesUnidadePort;
    private final GerarGraficoPizzaProdutoPort gerarGraficoPizzaProdutoPort;
    private final GerarRelatorioPort gerarRelatorioPort;
    private final GerarGraficoPort gerarGraficoPort;

    @Value("${digitalocean.storage.base-path:/olivander/ficha_tecnica/imagens}")
    private String storagePath;

    @Value("${digitalocean.storage.public-url:http://localhost:8080/uploads}")
    private String publicUrl;

    public ProdutoController(
            BuscarProdutoPort buscarProdutoPort,
            CriarProdutoPort criarProdutoPort,
            AtualizarProdutoPort atualizarProdutoPort,
            DeletarProdutoPort deletarProdutoPort,
            ListarItensDoProdutoPort listarItensDoProdutoPort,
            ObterDescricoesUnidadePort obterDescricoesUnidadePort,
            GerarGraficoPizzaProdutoPort gerarGraficoPizzaProdutoPort,
            GerarRelatorioPort gerarRelatorioPort,
            GerarGraficoPort gerarGraficoPort) {
        this.buscarProdutoPort = buscarProdutoPort;
        this.criarProdutoPort = criarProdutoPort;
        this.atualizarProdutoPort = atualizarProdutoPort;
        this.deletarProdutoPort = deletarProdutoPort;
        this.listarItensDoProdutoPort = listarItensDoProdutoPort;
        this.obterDescricoesUnidadePort = obterDescricoesUnidadePort;
        this.gerarGraficoPizzaProdutoPort = gerarGraficoPizzaProdutoPort;
        this.gerarRelatorioPort = gerarRelatorioPort;
        this.gerarGraficoPort = gerarGraficoPort;
    }

    @GetMapping("/produtos")
    @Operation(summary = "Lista produtos", description = "Retorna todos os produtos cadastrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhum produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Erro ao consultar produtos")
    })
    public ResponseEntity<List<ProdutoDTO>> buscarLista() {
        logger.info("Inicio do metodo buscarLista");
        try {
            List<ProdutoDTO> produtos = buscarProdutoPort.listar();
            if (produtos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(produtos);
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao buscar lista de produtos", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/produtos")
    @Transactional
    @Operation(summary = "Cadastra produto", description = "Cria um novo produto na base de dados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para cadastro")
    })
    public ResponseEntity<ProdutoDTO> cadastrarProduto(@RequestBody ProdutoDTO produto) {
        try {
            return ResponseEntity.ok(criarProdutoPort.cadastrarProduto(produto));
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao cadastrar produto", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/produtos/gerar-pdf-lista")
    @Operation(summary = "Gera PDF da lista de produtos", description = "Exporta a lista completa de produtos em PDF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhum produto encontrado para o relatorio"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos para geracao do PDF"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o relatorio")
    })
    public ResponseEntity<byte[]> gerarPdfLista() {
        logger.info("Inicio do metodo gerarPdfLista - ProdutoController");
        try {
            List<ProdutoDTO> lista = buscarProdutoPort.listar();
            if (lista.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            String jsonData = new Gson().toJson(lista);
            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("nome", "Nome");
            colunas.put("descricao", "Descricao");
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

            byte[] pdfBytes = gerarRelatorioPort.gerarRelatorioPDF(request);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Lista-Produtos-" + timestamp + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, TextoEncodingUtils.contentDispositionAttachment(filename))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (IllegalArgumentException e) {
            logger.error("Parametros invalidos para geracao do PDF de produtos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF de lista de produtos", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/produtos/gerar-pdf-detalhe/{id:[0-9]+}")
    @Operation(summary = "Gera PDF detalhado do produto", description = "Exporta a ficha detalhada de um produto especifico em PDF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos para geracao do PDF"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o relatorio")
    })
    public ResponseEntity<byte[]> gerarPdfDetalhe(@PathVariable Long id) {
        logger.info("Inicio do metodo gerarPdfDetalhe - ProdutoController - id: {}", id);
        try {
            ProdutoDTO produto = buscarProdutoPort.buscarPorId(id);
            List<ItemProduto> itensProduto = listarItensDoProdutoPort.listar(id);
            List<ProdutoCompletoDTO> itensProdutoDTO = itensProduto.stream().map(this::toProdutoCompletoDTO).toList();
            List<Long> codigosUnidade = itensProdutoDTO.stream()
                    .map(ProdutoCompletoDTO::cdUnidade)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            Map<Long, String> descricoesUnidade = obterDescricoesUnidadePort.obter(codigosUnidade);

            String jsonData = new Gson().toJson(List.of(montarRegistroRelatorio(produto, itensProdutoDTO, descricoesUnidade)));

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("nome", "Nome");
            colunas.put("descricao", "Descricao");
            colunas.put("valorVenda", "Valor de Venda");
            colunas.put("valorItens", "Valor dos Itens");

            byte[] imagemProdutoBytes = carregarImagemProduto(produto.imagem(), id);
            byte[] graficoPngBytes = null;

            try {
                GraficoPizzaDTO graficoDTO = gerarGraficoPizzaProdutoPort.gerar(id);
                if (graficoDTO != null && graficoDTO.valores() != null && !graficoDTO.valores().isEmpty()) {
                    graficoPngBytes = gerarGraficoPort.gerarGraficoPizzaPNG(graficoDTO);
                    logger.info("Grafico de composicao de custo gerado para produto id={}", id);
                }
            } catch (FichaTecnicaException ex) {
                logger.info("Sem composicao suficiente para grafico no produto id={} - PDF seguira sem grafico", id);
            } catch (Exception ex) {
                logger.warn("Falha ao gerar grafico de composicao do produto id={}: {}", id, ex.getMessage());
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

            byte[] pdfBytes = gerarRelatorioPort.gerarRelatorioPDF(request);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Detalhe-Produto-" + id + "-" + timestamp + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, TextoEncodingUtils.contentDispositionAttachment(filename))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (FichaTecnicaException e) {
            logger.error("Produto nao encontrado para id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("Parametros invalidos para geracao do PDF de detalhe de Produto: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF de detalhe de Produto", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/produtos/{id:[0-9]+}")
    @Operation(summary = "Busca produto por ID", description = "Retorna os dados de um produto especifico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "204", description = "Produto nao encontrado"),
            @ApiResponse(responseCode = "404", description = "Erro ao buscar produto")
    })
    public ResponseEntity<ProdutoDTO> buscarPorId(@PathVariable Long id) {
        try {
            ProdutoDTO produto = buscarProdutoPort.buscarPorId(id);
            if (produto == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(produto);
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao buscar produto por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/produtos/{id:[0-9]+}")
    @Transactional
    @Operation(summary = "Atualiza produto", description = "Altera os dados de um produto existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado")
    })
    public ResponseEntity<ProdutoDTO> atualizarProduto(@PathVariable Long id, @RequestBody ProdutoDTO produto) {
        try {
            return ResponseEntity.ok(atualizarProdutoPort.atualizarProduto(id, produto));
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao atualizar unidade de medida por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/produtos/{id:[0-9]+}")
    @Transactional
    @Operation(summary = "Remove produto", description = "Exclui um produto existente pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado")
    })
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        try {
            deletarProdutoPort.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao apagar produto por id", e);
            return ResponseEntity.notFound().build();
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

    private ProdutoCompletoDTO toProdutoCompletoDTO(ItemProduto itemProduto) {
        return new ProdutoCompletoDTO(
                itemProduto.getProduto() != null ? itemProduto.getProduto().getNome() : null,
                itemProduto.getItem() != null ? itemProduto.getItem().getNome() : null,
                itemProduto.getItem() != null ? itemProduto.getItem().getCodigo() : null,
                itemProduto.getQuantidade(),
                itemProduto.getUnidadePara() != null ? itemProduto.getUnidadePara().getCodigo() : null,
                itemProduto.getValor());
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
            logger.warn("Arquivo de imagem nao encontrado para produto id={}: {}", idProduto, imagePath);
        } catch (Exception e) {
            logger.warn("Nao foi possivel carregar a imagem do produto id={}: {}", idProduto, e.getMessage());
        }
        return null;
    }
}
