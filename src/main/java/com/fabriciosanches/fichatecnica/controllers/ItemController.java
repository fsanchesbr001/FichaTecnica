package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemDTO;
import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.enums.ImagemPosicao;
import com.fabriciosanches.fichatecnica.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.enums.TipoRelatorio;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.GraficoService;
import com.fabriciosanches.fichatecnica.services.HistoricoItemService;
import com.fabriciosanches.fichatecnica.services.ItemService;
import com.fabriciosanches.fichatecnica.services.RelatorioService;
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
public class ItemController {

    private static final Logger logger = LogManager.getLogger(ItemController.class);

    final ItemService itemService;
    final RelatorioService relatorioService;
    final HistoricoItemService historicoItemService;
    final GraficoService graficoService;

    public ItemController(ItemService itemService, RelatorioService relatorioService,
                          HistoricoItemService historicoItemService, GraficoService graficoService) {
        this.itemService = itemService;
        this.relatorioService = relatorioService;
        this.historicoItemService = historicoItemService;
        this.graficoService = graficoService;
    }

    @GetMapping("/itens")
    public ResponseEntity<List<ItemDTO>> buscarLista(){
        logger.info("Inicio do método buscarLista");
        logger.info("Buscando lista de itens");
        try {
            List<ItemDTO> itens = itemService.listar();
            if (itens.isEmpty()) {
                logger.error("Lista de itens não encontrada");
                return ResponseEntity.noContent().build();
            }
            logger.info("Lista de itens encontrada: {}", itens);
            logger.info("Fim do método buscarLista");
            return ResponseEntity.ok(itens);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar lista de itens", e);
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/itens/{id}")
    public ResponseEntity<ItemDTO> buscarPorId(@PathVariable Long id){
        logger.info("Inicio do método buscarPorId");
        logger.info("Buscando item por id: {}", id);
        try {
            ItemDTO item = itemService.buscarPorId(id);
            if (item == null) {
                logger.error("Item não encontrado");
                return ResponseEntity.noContent().build();
            }
            logger.info("Item encontrado: {}", item);
            logger.info("Fim do método buscarPorId");
            return ResponseEntity.ok(item);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar item por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/itens/{id}")
    @Transactional
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        logger.info("Inicio do método apagar");
        logger.info("Apagando item por id: {}", id);
        try {
            itemService.deletarItem(id);
            logger.info("Item apagado com sucesso");
            logger.info("Fim do método apagar");
            return ResponseEntity.noContent().build();
        }
        catch (FichaTecnicaException e){
            logger.error("Existem Históricos para o Item " + id);
            return ResponseEntity.unprocessableEntity().build();
        }
        catch (Exception e){
            logger.error("Erro ao apagar item por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/itens/{id}")
    @Transactional
    public ResponseEntity<ItemDTO> atualizarItem(@PathVariable Long id, @RequestBody ItemDTO itemDTO) {
        logger.info("Inicio do método atualizarItem");
        logger.info("Atualizando item por id: {}", id);
        try {
            ItemDTO item = itemService.atualizarItem(id, itemDTO);
            logger.info("Item atualizado com sucesso: {}", item);
            logger.info("Fim do método atualizarUnidade");
            return ResponseEntity.ok(item);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao atualizar unidade de medida por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/itens")
    @Transactional
    public ResponseEntity<ItemDTO> cadastrarItem(@RequestBody ItemDTO itemDTO) {
        logger.info("Inicio do método cadastrarItem");
        logger.info("Cadastrando Item: {}", itemDTO);
        try {
            ItemDTO item = itemService.cadastrarItem(itemDTO);
            logger.info("Item cadastrado com sucesso: {}", item);
            logger.info("Fim do método cadastrarItem");
            return ResponseEntity.ok(item);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao cadastrar item", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gera um PDF com a lista completa de Itens ordenados por nome.
     * Colunas exibidas: Nome, Unidade de Medida e Valor.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/itens/gerar-pdf-lista")
    public ResponseEntity<byte[]> gerarPdfLista() {
        logger.info("Início do método gerarPdfLista – ItemController");
        try {
            List<ItemDTO> lista = itemService.listar();

            if (lista.isEmpty()) {
                logger.warn("Nenhum item encontrado para gerar o relatório");
                return ResponseEntity.noContent().build();
            }

            String jsonData = new Gson().toJson(lista);

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("nome",          "Nome");
            colunas.put("unidadeMedida", "Unidade de Medida");
            colunas.put("valor",         "Valor");

            RelatorioRequestDTO request = new RelatorioRequestDTO(
                    jsonData,
                    "",
                    "Lista de Itens",
                    colunas,
                    TipoRelatorio.LISTA,
                    OrientacaoRelatorio.RETRATO,
                    true
            );

            byte[] pdfBytes = relatorioService.gerarRelatorioPDF(request);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Lista-Itens-" + timestamp + ".pdf";

            logger.info("PDF de lista de Itens gerado com sucesso – arquivo: '{}'", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            logger.error("Parâmetros inválidos para geração do PDF de lista de Itens: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF de lista de Itens", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Gera um PDF detalhado para um Item específico, identificado por {id}.
     * O relatório exibe todos os campos do item no formato de ficha (DETALHE / PAISAGEM).
     * Caso existam registros de histórico de preços, um gráfico de variação de preços
     * (JFreeChart – linha azul, pontos vermelhos) é adicionado ao final do relatório,
     * antes do rodapé.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/itens/gerar-pdf-detalhe/{id:[0-9]+}")
    public ResponseEntity<byte[]> gerarPdfDetalhe(@PathVariable Long id) {
        logger.info("Início do método gerarPdfDetalhe – ItemController – id: {}", id);
        try {
            ItemDTO item = itemService.buscarPorId(id);

            String jsonData = new Gson().toJson(List.of(item));

            Map<String, String> colunas = new LinkedHashMap<>();
            colunas.put("nome",          "Nome");
            colunas.put("unidadeMedida", "Unidade de Medida");
            colunas.put("valor",         "Valor");

            // Tenta gerar o gráfico de variação de preços (opcional – ignora se não houver histórico)
            byte[] graficoPng = null;
            try {
                GraficoPrecoItemDTO graficoDTO = historicoItemService.gerarGraficoPreco(id);
                if (graficoDTO != null && !graficoDTO.labels().isEmpty()) {
                    graficoPng = graficoService.gerarGraficoPNG(graficoDTO);
                    logger.info("Gráfico de preços gerado para inclusão no PDF – item id={}", id);
                }
            } catch (FichaTecnicaException ex) {
                logger.info("Sem histórico de preços para o item id={} – PDF será gerado sem gráfico", id);
            }

            RelatorioRequestDTO request;
            if (graficoPng != null) {
                request = new RelatorioRequestDTO(
                        jsonData,
                        "",
                        "Detalhe do Item",
                        colunas,
                        TipoRelatorio.DETALHE,
                        OrientacaoRelatorio.PAISAGEM,
                        false,
                        true,
                        graficoPng,
                        ImagemPosicao.FIM
                );
            } else {
                request = new RelatorioRequestDTO(
                        jsonData,
                        "",
                        "Detalhe do Item",
                        colunas,
                        TipoRelatorio.DETALHE,
                        OrientacaoRelatorio.PAISAGEM,
                        false
                );
            }

            byte[] pdfBytes = relatorioService.gerarRelatorioPDF(request);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "Detalhe-Item-" + id + "-" + timestamp + ".pdf";

            logger.info("PDF de detalhe de Item gerado com sucesso – arquivo: '{}'", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (FichaTecnicaException e) {
            logger.error("Item não encontrado para id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("Parâmetros inválidos para geração do PDF de detalhe de Item: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF de detalhe de Item", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
