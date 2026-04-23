package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
import com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.HistoricoItemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("ficha-tecnica")
public class HistoricoItemController {

    private static final Logger logger = LogManager.getLogger(HistoricoItemController.class);

    final HistoricoItemService historicoItemService;

    public HistoricoItemController(HistoricoItemService historicoItemService) {
        this.historicoItemService = historicoItemService;
    }

    @GetMapping("/historico-itens")
    public ResponseEntity<List<HistoricoItemDTO>> buscarLista(){
        logger.info("Inicio do método buscarLista");
        logger.info("Buscando lista de historico de itens");
        try {
            List<HistoricoItemDTO> historicoItemDTOList = historicoItemService.listar();
            logger.info("Lista de historico de itens encontrada: {}", historicoItemDTOList);
            logger.info("Fim do método buscarLista");
            return ResponseEntity.ok(historicoItemDTOList);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar lista de historico de itens", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/historico-itens/{id}")
    public ResponseEntity<HistoricoItemDTO> buscarPorId(@PathVariable Long id){
        logger.info("Inicio do método buscarPorId");
        logger.info("Buscando item por id: {}", id);
        try {
            HistoricoItemDTO historicoItem = historicoItemService.buscarPorId(id);
            logger.info("Item encontrado: {}", historicoItem);
            logger.info("Fim do método buscarPorId");
            return ResponseEntity.ok(historicoItem);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar item por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/historico-itens/itens/{id}")
    public ResponseEntity<List<HistoricoItemDTO>> buscarPorItemId(@PathVariable Long id){
        logger.info("Inicio do método buscarItemPorId");
        logger.info("Buscando item por id: {}", id);
        try {
            List<HistoricoItemDTO> listaHistoricoItem = historicoItemService.buscarPorCodigoItem(id);
            logger.info("Item encontrado: {}", listaHistoricoItem);
            logger.info("Fim do método buscarPorId");
            return ResponseEntity.ok(listaHistoricoItem);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar item por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retorna os dados de variação de preços de um item no formato Chart.js (ng2-charts).
     * <p>
     * Recebe o <b>código do Item</b> (campo {@code cdItem} no histórico) e retorna todos os
     * registros daquele item ordenados por data, com variação percentual entre pontos consecutivos.
     * </p>
     *
     * @param codigoItem código do Item (não o id da linha de histórico)
     * @return {@link GraficoPrecoItemDTO} com os dados do gráfico
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/historico-itens/grafico-precos/{codigoItem}")
    public ResponseEntity<GraficoPrecoItemDTO> gerarGraficoPrecos(@PathVariable Long codigoItem) {
        logger.info("Início do método gerarGraficoPrecos – codigoItem={}", codigoItem);
        try {
            GraficoPrecoItemDTO grafico = historicoItemService.gerarGraficoPreco(codigoItem);
            logger.info("Gráfico de preços gerado com sucesso para codigoItem={}", codigoItem);
            return ResponseEntity.ok(grafico);
        } catch (FichaTecnicaException e) {
            logger.warn("Nenhum histórico encontrado para codigoItem={}: {}", codigoItem, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar gráfico de preços para codigoItem={}", codigoItem, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
