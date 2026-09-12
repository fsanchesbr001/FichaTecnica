package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ConversaoDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ConversaoRelatorioDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.core.domain.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.core.domain.enums.TipoRelatorio;
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
import org.springframework.web.bind.annotation.*;

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
@Tag(name = "ConversÃµes", description = "Cadastro, consulta, atualizaÃ§Ã£o, exclusÃ£o e relatÃ³rios de conversÃµes de unidades")
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
    @Operation(summary = "Lista conversÃµes", description = "Retorna todas as conversÃµes cadastradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhuma conversÃ£o encontrada")
    })
    public ResponseEntity<List<ConversaoRelatorioDTO>> buscarLista() {
        logger.info("Inicio do mÃ©todo buscarLista");
        List<ConversaoRelatorioDTO> conversoes = gerarRelatorioConversaoPort.buscarTodosComNomes();
        if (conversoes.isEmpty()) {
            logger.warn("Lista de conversÃµes nÃ£o encontrada");
            return ResponseEntity.noContent().build();
        }
        logger.info("Fim do mÃ©todo buscarLista");
        return ResponseEntity.ok(conversoes);
    }

    @GetMapping("/conversoes/{id:[0-9]+}")
    @Operation(summary = "Busca conversÃ£o por ID", description = "Retorna os dados de uma conversÃ£o especÃ­fica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ConversÃ£o encontrada"),
            @ApiResponse(responseCode = "404", description = "ConversÃ£o nÃ£o encontrada")
    })
    public ResponseEntity<ConversaoDTO> buscarPorId(@PathVariable Long id) {
        logger.info("Inicio do mÃ©todo buscarPorId");
        try {
            Conversao conversao = buscarConversaoPort.buscarPorId(id);
            logger.info("Fim do mÃ©todo buscarPorId");
            return ResponseEntity.ok(toDto(conversao));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/conversoes/{id:[0-9]+}")
    @Operation(summary = "Remove conversÃ£o", description = "Exclui uma conversÃ£o existente pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "ConversÃ£o removida com sucesso")
    })
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        logger.info("Inicio do mÃ©todo apagar");
        try {
            deletarConversaoPort.deletar(id);
            logger.info("Fim do mÃ©todo apagar");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/conversoes/{id:[0-9]+}")
    @Transactional
    @Operation(summary = "Atualiza conversÃ£o", description = "Altera os dados de uma conversÃ£o existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ConversÃ£o atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "ConversÃ£o nÃ£o encontrada")
    })
    public ResponseEntity<ConversaoDTO> atualizarConversao(@PathVariable Long id, @RequestBody ConversaoDTO conversao) {
        logger.info("Inicio do mÃ©todo atualizarConversao");
        try {
            Conversao conversaoAtualizada = atualizarConversaoPort.atualizar(id, toDomain(conversao));
            logger.info("Fim do mÃ©todo atualizarConversao");
            return ResponseEntity.ok(toDto(conversaoAtualizada));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/conversoes")
    @Transactional
    @Operation(summary = "Cadastra conversÃ£o", description = "Cria uma nova conversÃ£o entre unidades.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ConversÃ£o cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invÃ¡lidos para cadastro")
    })
    public ResponseEntity<ConversaoDTO> cadastrarConversao(@RequestBody ConversaoDTO conversao) {
        logger.info("Inicio do mÃ©todo cadastrarConversao");
        try {
            Conversao conversaoCriada = criarConversaoPort.criar(toDomain(conversao));
            logger.info("Fim do mÃ©todo cadastrarConversao");
            return ResponseEntity.ok(toDto(conversaoCriada));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/conversoes/gerar-pdf-lista")
    @Operation(summary = "Gera PDF da lista de conversÃµes", description = "Exporta a lista completa de conversÃµes em PDF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhuma conversÃ£o encontrada para o relatÃ³rio"),
            @ApiResponse(responseCode = "400", description = "ParÃ¢metros invÃ¡lidos para geraÃ§Ã£o do PDF"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o relatÃ³rio")
    })
    public ResponseEntity<byte[]> gerarPdfLista() {
        logger.info("InÃ­cio do mÃ©todo gerarPdfLista â€“ ConversaoController");
        try {
            List<ConversaoRelatorioDTO> lista = gerarRelatorioConversaoPort.buscarTodosComNomes();

            if (lista.isEmpty()) {
                logger.warn("Nenhuma conversÃ£o encontrada para gerar o relatÃ³rio");
                return ResponseEntity.noContent().build();
            }

            String jsonData = GSON_BR.toJson(lista);

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("unidadeDe", "De");
            colunas.put("unidadePara", "Para");
            colunas.put("operacao", "OperaÃ§Ã£o");
            colunas.put("valor", "Valor");

            RelatorioRequestDTO request = new RelatorioRequestDTO(
                    jsonData, "",
                    "Lista de ConversÃµes",
                    colunas,
                    TipoRelatorio.LISTA,
                    OrientacaoRelatorio.RETRATO,
                    true
            );

            byte[] pdfBytes = gerarRelatorioPort.gerarRelatorioPDF(request);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Lista-Conversoes-" + timestamp + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
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
    @Operation(summary = "Gera PDF detalhado da conversÃ£o", description = "Exporta a ficha detalhada de uma conversÃ£o especÃ­fica em PDF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "ConversÃ£o nÃ£o encontrada"),
            @ApiResponse(responseCode = "400", description = "ParÃ¢metros invÃ¡lidos para geraÃ§Ã£o do PDF"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o relatÃ³rio")
    })
    public ResponseEntity<byte[]> gerarPdfDetalhe(@PathVariable Long id) {
        logger.info("InÃ­cio do mÃ©todo gerarPdfDetalhe â€“ ConversaoController â€“ id: {}", id);
        try {
            ConversaoRelatorioDTO conversao = gerarRelatorioConversaoPort.buscarPorIdComNomes(id);

            String jsonData = GSON_BR.toJson(List.of(conversao));

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("unidadeDe", "Unidade De");
            colunas.put("unidadePara", "Unidade Para");
            colunas.put("operacao", "OperaÃ§Ã£o");
            colunas.put("valor", "Valor");

            RelatorioRequestDTO request = new RelatorioRequestDTO(
                    jsonData, "", "Detalhe da ConversÃ£o", colunas,
                    TipoRelatorio.DETALHE,
                    OrientacaoRelatorio.PAISAGEM,
                    false
            );

            byte[] pdfBytes = gerarRelatorioPort.gerarRelatorioPDF(request);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Detalhe-Conversao-" + id + "-" + timestamp + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
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

