package com.fabriciosanches.fichatecnica.controller;

import com.fabriciosanches.fichatecnica.domain.conversao.DadosConversao;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.ConversaoService;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("ficha-tecnica")
public class ConversaoController {
    private static final Logger logger = LogManager.getLogger(ConversaoController.class);

    final ConversaoService conversaoService;

    public ConversaoController(ConversaoService conversaoService) {
        this.conversaoService =conversaoService;
    }

    @GetMapping("/conversoes")
    public ResponseEntity<List<DadosConversao>> buscarLista(){
        logger.info("Inicio do método buscarLista");
        logger.info("Buscando lista de conversões");
        try {
            List<DadosConversao> conversoes = conversaoService.listar();
            logger.info("Lista de conversoes encontrada: {}", conversoes);
            logger.info("Fim do método buscarLista");
            return ResponseEntity.ok(conversoes);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar lista de conversoes", e);
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/conversoes/{id}")
    public ResponseEntity<DadosConversao> buscarPorId(@PathVariable Long id){
        logger.info("Inicio do método buscarPorId");
        logger.info("Buscando conversoes por id: {}", id);
        try {
            DadosConversao conversao = conversaoService.buscarPorId(id);
            logger.info("Conversao encontrada: {}", conversao);
            logger.info("Fim do método buscarPorId");
            return ResponseEntity.ok(conversao);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar conversao por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/conversoes/{id}")
    @Transactional
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

    @PutMapping("/conversoes/{id}")
    @Transactional
    public ResponseEntity<DadosConversao> atualizarConversao(@PathVariable Long id, @RequestBody DadosConversao conversao) {
        logger.info("Inicio do método atualizarConversao");
        logger.info("Atualizando conversao por id: {}", id);
        try {
            DadosConversao conversoes = conversaoService.atualizarConversao(id, conversao);
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
    public ResponseEntity<DadosConversao> cadastrarConversao(@RequestBody DadosConversao conversao) {
        logger.info("Inicio do método cadastrarConversao");
        logger.info("Cadastrando unidade de medida: {}", conversao);
        try {
            DadosConversao dadosConversao = conversaoService.cadastrarConversao(conversao);
            logger.info("Conversao cadastrada com sucesso: {}", dadosConversao);
            logger.info("Fim do método cadastrarConversao");
            return ResponseEntity.ok(dadosConversao);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao cadastrar conversao", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
