package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.ConversaoDTO;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresInputDTO;
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
    public ResponseEntity<List<ConversaoDTO>> buscarLista(){
        logger.info("Inicio do método buscarLista");
        logger.info("Buscando lista de conversões");
        try {
            List<ConversaoDTO> conversoes = conversaoService.listar();
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

    @GetMapping("/conversoes/{id}")
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

    @PostMapping("/conversoes/valores")
    public ResponseEntity<ConversaoValoresDTO> obterValoresConversao(
            @RequestBody ConversaoValoresInputDTO conversaoValoresInputDTO) {
        logger.info("Inicio do método obterValoresConversao");
        try {
            ConversaoValoresDTO valoresDTO = conversaoService.obterValoresConversao(conversaoValoresInputDTO.idItem(),
                    conversaoValoresInputDTO.quantidade(), conversaoValoresInputDTO.idUnidade());
            logger.info("Valores de conversão obtidos com sucesso: {}", valoresDTO);
            logger.info("Fim do método obterValoresConversao");
            return ResponseEntity.ok(valoresDTO);
        } catch (FichaTecnicaException e) {
            logger.error("Erro ao obter valores de conversão", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
