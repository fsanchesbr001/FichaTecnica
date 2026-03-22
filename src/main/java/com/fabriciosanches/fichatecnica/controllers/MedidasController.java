package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.UnidadeMedidaDTO;
import com.fabriciosanches.fichatecnica.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.enums.TipoRelatorio;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.RelatorioService;
import com.fabriciosanches.fichatecnica.services.UnidadeMedidaService;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("ficha-tecnica")
public class MedidasController {
    private static final Logger logger = LogManager.getLogger(MedidasController.class);

    final UnidadeMedidaService unidadeService;
    final RelatorioService relatorioService;

    public MedidasController(UnidadeMedidaService unidadeService, RelatorioService relatorioService) {
        this.unidadeService = unidadeService;
        this.relatorioService = relatorioService;
    }

    @GetMapping("/unidades-medida")
    public ResponseEntity<List<UnidadeMedidaDTO>> buscarLista(){
        logger.info("Inicio do método buscarLista");
        logger.info("Buscando lista de unidades de medida");
        try {
            List<UnidadeMedidaDTO> medidas = unidadeService.listar();
            logger.info("Lista de unidades de medida encontrada: {}", medidas);
            logger.info("Fim do método buscarLista");
            return ResponseEntity.ok(medidas);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar lista de unidades de medida", e);
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/unidades-medida/{id:[0-9]+}")
    public ResponseEntity<UnidadeMedidaDTO> buscarPorId(@PathVariable Long id){
        logger.info("Inicio do método buscarPorId");
        logger.info("Buscando unidade de medida por id: {}", id);
        try {
            UnidadeMedidaDTO medida = unidadeService.buscarPorId(id);
            logger.info("Unidade de medida encontrada: {}", medida);
            logger.info("Fim do método buscarPorId");
            return ResponseEntity.ok(medida);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar unidade de medida por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/unidades-medida/{id:[0-9]+}")
    public ResponseEntity<?> apagar(@PathVariable Long id) {
        logger.info("Inicio do método apagar");
        logger.info("Apagando unidade de medida por id: {}", id);
        try {
            unidadeService.deletarUnidade(id);
            logger.info("Unidade de medida apagada com sucesso");
            logger.info("Fim do método apagar");
            return ResponseEntity.noContent().build();
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao apagar unidade de medida por id {}: {}", id, e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/unidades-medida/{id:[0-9]+}")
    @Transactional
    public ResponseEntity<?> atualizarUnidade(@PathVariable Long id, @RequestBody UnidadeMedidaDTO unidade) {
        logger.info("Inicio do método atualizarUnidade");
        logger.info("Atualizando unidade de medida por id: {}", id);
        try {
            UnidadeMedidaDTO medida = unidadeService.atualizarUnidade(id, unidade);
            logger.info("Unidade de medida atualizada com sucesso: {}", medida);
            logger.info("Fim do método atualizarUnidade");
            return ResponseEntity.ok(medida);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao atualizar unidade de medida por id {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/unidades-medida")
    @Transactional
    public ResponseEntity<?> cadastrarUnidade(@RequestBody UnidadeMedidaDTO unidade) {
        logger.info("Inicio do método cadastrarUnidade");
        logger.info("Cadastrando unidade de medida: {}", unidade);
        try {
            UnidadeMedidaDTO medida = unidadeService.cadastrarUnidade(unidade);
            logger.info("Unidade de medida cadastrada com sucesso: {}", medida);
            logger.info("Fim do método cadastrarUnidade");
            return ResponseEntity.ok(medida);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao cadastrar unidade de medida", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Gera um PDF com a lista completa de Unidades de Medida ordenadas por nome.
     * Colunas exibidas: Código, Nome e Sigla.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/unidades-medida/gerar-pdf-lista")
    public ResponseEntity<byte[]> gerarPdfLista() {
        logger.info("Início do método gerarPdfLista – MedidasController");
        try {
            List<UnidadeMedidaDTO> lista = unidadeService.listar();

            if (lista.isEmpty()) {
                logger.warn("Nenhuma unidade de medida encontrada para gerar o relatório");
                return ResponseEntity.noContent().build();
            }

            String jsonData = new Gson().toJson(lista);

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("nome",  "Nome");
            colunas.put("sigla", "Sigla");

            RelatorioRequestDTO request = new RelatorioRequestDTO(
                    jsonData,
                    "",
                    "Lista de Unidades de Medida",
                    colunas,
                    TipoRelatorio.LISTA,
                    OrientacaoRelatorio.RETRATO,
                    true
            );

            byte[] pdfBytes = relatorioService.gerarRelatorioPDF(request);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Lista-Unidades-Medida-" + timestamp + ".pdf";

            logger.info("PDF de lista de Unidades de Medida gerado com sucesso – arquivo: '{}'", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            logger.error("Parâmetros inválidos para geração do PDF de lista: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF de lista de Unidades de Medida", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Gera um PDF detalhado para uma Unidade de Medida específica, identificada por {id}.
     * O relatório exibe todos os campos da unidade no formato de ficha (DETALHE / PAISAGEM).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/unidades-medida/gerar-pdf-detalhe/{id:[0-9]+}")
    public ResponseEntity<byte[]> gerarPdfDetalhe(@PathVariable Long id) {
        logger.info("Início do método gerarPdfDetalhe – MedidasController – id: {}", id);
        try {
            UnidadeMedidaDTO medida = unidadeService.buscarPorId(id);

            // Envolve o objeto em uma lista para o RelatorioService (espera List de registros)
            String jsonData = new Gson().toJson(List.of(medida));

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("nome",  "Nome");
            colunas.put("sigla", "Sigla");

            RelatorioRequestDTO request = new RelatorioRequestDTO(
                    jsonData,
                    "",
                    "Detalhe da Unidade de Medida",
                    colunas,
                    TipoRelatorio.DETALHE,
                    OrientacaoRelatorio.PAISAGEM,
                    false
            );

            byte[] pdfBytes = relatorioService.gerarRelatorioPDF(request);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Detalhe-Unidade-Medida-" + id + "-" + timestamp + ".pdf";

            logger.info("PDF de detalhe de Unidade de Medida gerado com sucesso – arquivo: '{}'", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (FichaTecnicaException e) {
            logger.error("Unidade de medida não encontrada para id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("Parâmetros inválidos para geração do PDF de detalhe: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF de detalhe de Unidade de Medida", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
