package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.HistoricoItemService;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/historico-itens/{id}")
    @Transactional
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        logger.info("Inicio do método apagar");
        logger.info("Apagando historico de item por id: {}", id);
        try {
            historicoItemService.deletarItem(id);
            logger.info("Historico do Item apagado com sucesso");
            logger.info("Fim do método apagar");
            return ResponseEntity.noContent().build();
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao apagar historico do item por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/historico-itens/itens/{codItem}")
    @Transactional
    public ResponseEntity<Void> apagarPorCodItem(@PathVariable Long codItem) {
        logger.info("Inicio do método apagarPorCodItem");
        logger.info("Apagando historico de item por codItem: {}", codItem);
        try {
            historicoItemService.deletarPorCodigoItem(codItem);
            logger.info("Historico do Item apagado com sucesso");
            logger.info("Fim do método apagarPorCodItem");
            return ResponseEntity.noContent().build();
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao apagar historico do item por id", e);
            return ResponseEntity.notFound().build();
        }
    }



    @PutMapping("/historico-itens/{id}")
    @Transactional
    public ResponseEntity<HistoricoItemDTO> atualizar(@PathVariable Long id, @RequestBody HistoricoItemDTO historicoItemDTO) {
        logger.info("Inicio do método atualizarItem");
        logger.info("Atualizando item por id: {}", id);
        try {
            HistoricoItemDTO item = historicoItemService.atualizarHistoricoItem(id, historicoItemDTO);
            logger.info("Item atualizado com sucesso: {}", item);
            logger.info("Fim do método atualizarUnidade");
            return ResponseEntity.ok(item);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao atualizar unidade de medida por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/historico-itens")
    @Transactional
    public ResponseEntity<HistoricoItemDTO> cadastrar(@RequestBody HistoricoItemDTO historicoItemDTO) {
        logger.info("Inicio do método cadastrar");
        logger.info("Cadastrando Historico do Item: {}", historicoItemDTO);
        try {
            HistoricoItemDTO dadosHistorico = historicoItemService.cadastrarItem(historicoItemDTO);
            logger.info("Historico de Item cadastrado com sucesso: {}", dadosHistorico);
            logger.info("Fim do método cadastrar");
            return ResponseEntity.ok(dadosHistorico);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao cadastrar historico do item", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
