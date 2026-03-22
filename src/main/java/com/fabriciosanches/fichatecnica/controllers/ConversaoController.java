package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.ConversaoDTO;
import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;
import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.enums.TipoRelatorio;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.ConversaoService;
import com.fabriciosanches.fichatecnica.services.RelatorioService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
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
public class ConversaoController {
    private static final Logger logger = LogManager.getLogger(ConversaoController.class);

    /** Gson com formatação de BigDecimal no padrão brasileiro (ex.: 1.234,56). */
    private static final Gson GSON_BR = new GsonBuilder()
            .registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>) (src, typeOfSrc, ctx) -> {
                NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
                fmt.setMinimumFractionDigits(2);
                fmt.setMaximumFractionDigits(2);
                return new JsonPrimitive(fmt.format(src));
            })
            .create();

    final ConversaoService conversaoService;
    final RelatorioService relatorioService;

    public ConversaoController(ConversaoService conversaoService, RelatorioService relatorioService) {
        this.conversaoService = conversaoService;
        this.relatorioService = relatorioService;
    }

    @GetMapping("/conversoes")
    public ResponseEntity<List<ConversaoRelatorioDTO>> buscarLista(){
        logger.info("Inicio do método buscarLista");
        logger.info("Buscando lista de conversões");
        try {
            List<ConversaoRelatorioDTO> conversoes = conversaoService.listarParaPdf();
            if (conversoes.isEmpty()) {
                logger.error("Lista de conversoes não encontrada");
                return ResponseEntity.noContent().build();
            }
            logger.info("Lista de conversoes encontrada: {}", conversoes);
            logger.info("Fim do método buscarLista");
            return ResponseEntity.ok(conversoes);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar lista de conversoes", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/conversoes/{id:[0-9]+}")
    public ResponseEntity<ConversaoDTO> buscarPorId(@PathVariable Long id){
        logger.info("Inicio do método buscarPorId");
        logger.info("Buscando conversoes por id: {}", id);
        try {
            ConversaoDTO conversao = conversaoService.buscarPorId(id);
            logger.info("Conversao encontrada: {}", conversao);
            logger.info("Fim do método buscarPorId");
            return ResponseEntity.ok(conversao);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar conversao por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/conversoes/{id:[0-9]+}")
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        logger.info("Inicio do método apagar");
        logger.info("Apagando conversoes por id: {}", id);
        try {
            conversaoService.deletarConversao(id);
            logger.info("Conversao apagada com sucesso");
            logger.info("Fim do método apagar");
            return ResponseEntity.noContent().build();
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao apagar conversao por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/conversoes/{id:[0-9]+}")
    @Transactional
    public ResponseEntity<ConversaoDTO> atualizarConversao(@PathVariable Long id, @RequestBody ConversaoDTO conversao) {
        logger.info("Inicio do método atualizarConversao");
        logger.info("Atualizando conversao por id: {}", id);
        try {
            ConversaoDTO conversoes = conversaoService.atualizarConversao(id, conversao);
            logger.info("Conversao atualizada com sucesso: {}", conversoes);
            logger.info("Fim do método atualizarConversao");
            return ResponseEntity.ok(conversoes);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao atualizar conversao por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/conversoes")
    @Transactional
    public ResponseEntity<ConversaoDTO> cadastrarConversao(@RequestBody ConversaoDTO conversao) {
        logger.info("Inicio do método cadastrarConversao");
        logger.info("Cadastrando unidade de medida: {}", conversao);
        try {
            ConversaoDTO conversaoResponseDTO = conversaoService.cadastrarConversao(conversao);
            logger.info("Conversao cadastrada com sucesso: {}", conversaoResponseDTO);
            logger.info("Fim do método cadastrarConversao");
            return ResponseEntity.ok(conversaoResponseDTO);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao cadastrar conversao", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gera um PDF com a lista completa de Conversões ordenadas por Unidade De.
     * Os nomes das unidades são resolvidos via JPQL JOIN (sem chamadas extras ao serviço).
     * Colunas exibidas: Unidade De, Unidade Para, Operação e Valor.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/conversoes/gerar-pdf-lista")
    public ResponseEntity<byte[]> gerarPdfLista() {
        logger.info("Início do método gerarPdfLista – ConversaoController");
        try {
            List<ConversaoRelatorioDTO> lista = conversaoService.listarParaPdf();

            if (lista.isEmpty()) {
                logger.warn("Nenhuma conversão encontrada para gerar o relatório");
                return ResponseEntity.noContent().build();
            }

            String jsonData = GSON_BR.toJson(lista);

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("unidadeDe",   "De");
            colunas.put("unidadePara", "Para");
            colunas.put("operacao",    "Operação");
            colunas.put("valor",       "Valor");

            RelatorioRequestDTO request = new RelatorioRequestDTO(
                    jsonData, "",
                    "Lista de Conversões",
                    colunas,
                    TipoRelatorio.LISTA,
                    OrientacaoRelatorio.RETRATO,
                    true
            );

            byte[] pdfBytes = relatorioService.gerarRelatorioPDF(request);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Lista-Conversoes-" + timestamp + ".pdf";

            logger.info("PDF de lista de Conversões gerado com sucesso – arquivo: '{}'", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            logger.error("Parâmetros inválidos para geração do PDF de lista: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF de lista de Conversões", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Gera um PDF detalhado para uma Conversão específica, identificada por {id}.
     * Os nomes das unidades são resolvidos via JPQL JOIN (sem chamadas extras ao serviço).
     * O relatório exibe todos os campos no formato de ficha (DETALHE / PAISAGEM).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/conversoes/gerar-pdf-detalhe/{id:[0-9]+}")
    public ResponseEntity<byte[]> gerarPdfDetalhe(@PathVariable Long id) {
        logger.info("Início do método gerarPdfDetalhe – ConversaoController – id: {}", id);
        try {
            ConversaoRelatorioDTO conversao = conversaoService.buscarPorIdParaPdf(id);

            String jsonData = GSON_BR.toJson(List.of(conversao));

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("unidadeDe",   "Unidade De");
            colunas.put("unidadePara", "Unidade Para");
            colunas.put("operacao",    "Operação");
            colunas.put("valor",       "Valor");

            RelatorioRequestDTO request = new RelatorioRequestDTO(
                    jsonData, "", "Detalhe da Conversão", colunas,
                    TipoRelatorio.DETALHE,
                    OrientacaoRelatorio.PAISAGEM,
                    false
            );

            byte[] pdfBytes = relatorioService.gerarRelatorioPDF(request);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Detalhe-Conversao-" + id + "-" + timestamp + ".pdf";

            logger.info("PDF de detalhe de Conversão gerado com sucesso – arquivo: '{}'", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (FichaTecnicaException e) {
            logger.error("Conversão não encontrada para id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("Parâmetros inválidos para geração do PDF de detalhe: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF de detalhe de Conversão", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
