package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.domain.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.core.domain.enums.TipoRelatorio;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ConversaoDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ConversaoRelatorioDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.infrastructure.util.TextoEncodingUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("ficha-tecnica")
@Tag(name = "Conversoes", description = "Cadastro, consulta, atualizacao, exclusao e relatorios de conversoes de unidades")
@SecurityRequirement(name = "bearerAuth")
public class ConversaoController {
    private static final Logger logger = LogManager.getLogger(ConversaoController.class);

    private static final Gson GSON_BR = new GsonBuilder()
            .registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>) (src, typeOfSrc, ctx) -> {
                NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
                fmt.setMinimumFractionDigits(2);
                fmt.setMaximumFractionDigits(2);
                return new JsonPrimitive(fmt.format(src));
            })
            .create();

    private final BuscarConversaoPort buscarConversaoPort;
    private final CriarConversaoPort criarConversaoPort;
    private final AtualizarConversaoPort atualizarConversaoPort;
    private final DeletarConversaoPort deletarConversaoPort;
    private final GerarRelatorioConversaoPort gerarRelatorioConversaoPort;
    private final GerarRelatorioPort gerarRelatorioPort;

    public ConversaoController(BuscarConversaoPort buscarConversaoPort,
                               CriarConversaoPort criarConversaoPort,
                               AtualizarConversaoPort atualizarConversaoPort,
                               DeletarConversaoPort deletarConversaoPort,
                               GerarRelatorioConversaoPort gerarRelatorioConversaoPort,
                               GerarRelatorioPort gerarRelatorioPort) {
        this.buscarConversaoPort = buscarConversaoPort;
        this.criarConversaoPort = criarConversaoPort;
        this.atualizarConversaoPort = atualizarConversaoPort;
        this.deletarConversaoPort = deletarConversaoPort;
        this.gerarRelatorioConversaoPort = gerarRelatorioConversaoPort;
        this.gerarRelatorioPort = gerarRelatorioPort;
    }

    @GetMapping("/conversoes")
    @Operation(summary = "Lista conversoes", description = "Retorna todas as conversoes cadastradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhuma conversao encontrada")
    })
    public ResponseEntity<List<ConversaoRelatorioDTO>> buscarLista() {
        logger.info("Inicio do metodo buscarLista");
        List<ConversaoRelatorioDTO> conversoes = gerarRelatorioConversaoPort.buscarTodosComNomes();
        if (conversoes.isEmpty()) {
            logger.warn("Lista de conversoes nao encontrada");
            return ResponseEntity.noContent().build();
        }
        logger.info("Fim do metodo buscarLista");
        return ResponseEntity.ok(conversoes);
    }

    @GetMapping("/conversoes/{id:[0-9]+}")
    @Operation(summary = "Busca conversao por ID", description = "Retorna os dados de uma conversao especifica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversao encontrada"),
            @ApiResponse(responseCode = "404", description = "Conversao nao encontrada")
    })
    public ResponseEntity<ConversaoDTO> buscarPorId(@PathVariable Long id) {
        logger.info("Inicio do metodo buscarPorId");
        try {
            Conversao conversao = buscarConversaoPort.buscarPorId(id);
            logger.info("Fim do metodo buscarPorId");
            return ResponseEntity.ok(toDto(conversao));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/conversoes/{id:[0-9]+}")
    @Operation(summary = "Remove conversao", description = "Exclui uma conversao existente pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conversao removida com sucesso")
    })
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        logger.info("Inicio do metodo apagar");
        try {
            deletarConversaoPort.deletar(id);
            logger.info("Fim do metodo apagar");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/conversoes/{id:[0-9]+}")
    @Transactional
    @Operation(summary = "Atualiza conversao", description = "Altera os dados de uma conversao existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversao atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conversao nao encontrada")
    })
    public ResponseEntity<ConversaoDTO> atualizarConversao(@PathVariable Long id, @RequestBody ConversaoDTO conversao) {
        logger.info("Inicio do metodo atualizarConversao");
        try {
            Conversao conversaoAtualizada = atualizarConversaoPort.atualizar(id, toDomain(conversao));
            logger.info("Fim do metodo atualizarConversao");
            return ResponseEntity.ok(toDto(conversaoAtualizada));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/conversoes")
    @Transactional
    @Operation(summary = "Cadastra conversao", description = "Cria uma nova conversao entre unidades.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversao cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos para cadastro")
    })
    public ResponseEntity<ConversaoDTO> cadastrarConversao(@RequestBody ConversaoDTO conversao) {
        logger.info("Inicio do metodo cadastrarConversao");
        try {
            Conversao conversaoCriada = criarConversaoPort.criar(toDomain(conversao));
            logger.info("Fim do metodo cadastrarConversao");
            return ResponseEntity.ok(toDto(conversaoCriada));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/conversoes/gerar-pdf-lista")
    @Operation(summary = "Gera PDF da lista de conversoes", description = "Exporta a lista completa de conversoes em PDF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhuma conversao encontrada para o relatorio"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos para geracao do PDF"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o relatorio")
    })
    public ResponseEntity<byte[]> gerarPdfLista() {
        logger.info("Inicio do metodo gerarPdfLista - ConversaoController");
        try {
            List<ConversaoRelatorioDTO> lista = gerarRelatorioConversaoPort.buscarTodosComNomes();

            if (lista.isEmpty()) {
                logger.warn("Nenhuma conversao encontrada para gerar o relatorio");
                return ResponseEntity.noContent().build();
            }

            String jsonData = GSON_BR.toJson(lista);

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("unidadeDe", "De");
            colunas.put("unidadePara", "Para");
            colunas.put("operacao", "Operacao");
            colunas.put("valor", "Valor");

            RelatorioRequestDTO request = new RelatorioRequestDTO(
                    jsonData,
                    "",
                    "Lista de Conversoes",
                    colunas,
                    TipoRelatorio.LISTA,
                    OrientacaoRelatorio.RETRATO,
                    true
            );

            byte[] pdfBytes = gerarRelatorioPort.gerarRelatorioPDF(request);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Lista-Conversoes-" + timestamp + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, TextoEncodingUtils.contentDispositionAttachment(filename))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/conversoes/gerar-pdf-detalhe/{id:[0-9]+}")
    @Operation(summary = "Gera PDF detalhado da conversao", description = "Exporta a ficha detalhada de uma conversao especifica em PDF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conversao nao encontrada"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos para geracao do PDF"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o relatorio")
    })
    public ResponseEntity<byte[]> gerarPdfDetalhe(@PathVariable Long id) {
        logger.info("Inicio do metodo gerarPdfDetalhe - ConversaoController - id: {}", id);
        try {
            ConversaoRelatorioDTO conversao = gerarRelatorioConversaoPort.buscarPorIdComNomes(id);
            String jsonData = GSON_BR.toJson(List.of(conversao));

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("unidadeDe", "Unidade De");
            colunas.put("unidadePara", "Unidade Para");
            colunas.put("operacao", "Operacao");
            colunas.put("valor", "Valor");

            RelatorioRequestDTO request = new RelatorioRequestDTO(
                    jsonData,
                    "",
                    "Detalhe da Conversao",
                    colunas,
                    TipoRelatorio.DETALHE,
                    OrientacaoRelatorio.PAISAGEM,
                    false
            );

            byte[] pdfBytes = gerarRelatorioPort.gerarRelatorioPDF(request);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Detalhe-Conversao-" + id + "-" + timestamp + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, TextoEncodingUtils.contentDispositionAttachment(filename))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private Conversao toDomain(ConversaoDTO dto) {
        return new Conversao(dto.codigo(), dto.unidadeDe(), dto.unidadePara(), dto.operacao(), dto.valor());
    }

    private ConversaoDTO toDto(Conversao conversao) {
        return new ConversaoDTO(
                conversao.getCodigo(),
                conversao.getUnidadeDe(),
                conversao.getUnidadePara(),
                conversao.getOperacao(),
                conversao.getValor()
        );
    }
}
