package com.fabriciosanches.fichatecnica.controller;

import com.fabriciosanches.fichatecnica.domain.medidas.DadosUnidadeMedida;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.UnidadeMedidaService;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("ficha-tecnica")
public class MedidasController {
    private static final Logger logger = LogManager.getLogger(MedidasController.class);

    final UnidadeMedidaService unidadeService;

    public MedidasController(UnidadeMedidaService unidadeService) {
        this.unidadeService = unidadeService;
    }

    @GetMapping("/unidades-medida")
    public ResponseEntity<List<DadosUnidadeMedida>> buscarLista(){
        logger.info("Inicio do método buscarLista");
        logger.info("Buscando lista de unidades de medida");
        try {
            List<DadosUnidadeMedida> medidas = unidadeService.listar();
            logger.info("Lista de unidades de medida encontrada: {}", medidas);
            logger.info("Fim do método buscarLista");
            return ResponseEntity.ok(medidas);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar lista de unidades de medida", e);
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/unidades-medida/{id}")
    public ResponseEntity<DadosUnidadeMedida> buscarPorId(@PathVariable Long id){
        logger.info("Inicio do método buscarPorId");
        logger.info("Buscando unidade de medida por id: {}", id);
        try {
            DadosUnidadeMedida medida = unidadeService.buscarPorId(id);
            logger.info("Unidade de medida encontrada: {}", medida);
            logger.info("Fim do método buscarPorId");
            return ResponseEntity.ok(medida);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar unidade de medida por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/unidades-medida/{id}")
    @Transactional
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        logger.info("Inicio do método apagar");
        logger.info("Apagando unidade de medida por id: {}", id);
        try {
            unidadeService.deletarUnidade(id);
            logger.info("Unidade de medida apagada com sucesso");
            logger.info("Fim do método apagar");
            return ResponseEntity.noContent().build();
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao apagar unidade de medida por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/unidades-medida/{id}")
    @Transactional
    public ResponseEntity<DadosUnidadeMedida> atualizarUnidade(@PathVariable Long id, @RequestBody DadosUnidadeMedida unidade) {
        logger.info("Inicio do método atualizarUnidade");
        logger.info("Atualizando unidade de medida por id: {}", id);
        try {
            DadosUnidadeMedida medida = unidadeService.atualizarUnidade(id, unidade);
            logger.info("Unidade de medida atualizada com sucesso: {}", medida);
            logger.info("Fim do método atualizarUnidade");
            return ResponseEntity.ok(medida);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao atualizar unidade de medida por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/unidades-medida")
    @Transactional
    public ResponseEntity<DadosUnidadeMedida> cadastrarUnidade(@RequestBody DadosUnidadeMedida unidade) {
        logger.info("Inicio do método cadastrarUnidade");
        logger.info("Cadastrando unidade de medida: {}", unidade);
        try {
            DadosUnidadeMedida medida = unidadeService.cadastrarUnidade(unidade);
            logger.info("Unidade de medida cadastrada com sucesso: {}", medida);
            logger.info("Fim do método cadastrarUnidade");
            return ResponseEntity.ok(medida);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao cadastrar unidade de medida", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
