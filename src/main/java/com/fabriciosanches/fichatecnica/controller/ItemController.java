package com.fabriciosanches.fichatecnica.controller;

import com.fabriciosanches.fichatecnica.domain.itens.DadosItem;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.ItemService;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ficha-tecnica")
public class ItemController {

    private static final Logger logger = LogManager.getLogger(ItemController.class);

    final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/itens")
    public ResponseEntity<List<DadosItem>> buscarLista(){
        logger.info("Inicio do método buscarLista");
        logger.info("Buscando lista de itens");
        try {
            List<DadosItem> itens = itemService.listar();
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
    public ResponseEntity<DadosItem> buscarPorId(@PathVariable Long id){
        logger.info("Inicio do método buscarPorId");
        logger.info("Buscando item por id: {}", id);
        try {
            DadosItem item = itemService.buscarPorId(id);
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
            logger.error("Erro ao apagar item por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/itens/{id}")
    @Transactional
    public ResponseEntity<DadosItem> atualizarItem(@PathVariable Long id, @RequestBody DadosItem dadosItem) {
        logger.info("Inicio do método atualizarItem");
        logger.info("Atualizando item por id: {}", id);
        try {
            DadosItem item = itemService.atualizarItem(id, dadosItem);
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
    public ResponseEntity<DadosItem> cadastrarItem(@RequestBody DadosItem dadosItem) {
        logger.info("Inicio do método cadastrarItem");
        logger.info("Cadastrando Item: {}", dadosItem);
        try {
            DadosItem item = itemService.cadastrarItem(dadosItem);
            logger.info("Item cadastrado com sucesso: {}", item);
            logger.info("Fim do método cadastrarItem");
            return ResponseEntity.ok(item);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao cadastrar item", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
