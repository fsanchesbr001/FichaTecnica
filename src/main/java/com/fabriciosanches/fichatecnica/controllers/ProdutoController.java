package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.ProdutoService;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("ficha-tecnica")
public class ProdutoController {
    private static final Logger logger = LogManager.getLogger(ProdutoController.class);

    final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/produtos")
    public ResponseEntity<List<ProdutoDTO>> buscarLista(){
        logger.info("Inicio do método buscarLista");
        logger.info("Buscando lista de produto");
        try {
            List<ProdutoDTO> produtos = produtoService.listar();
            if (produtos.isEmpty()){
                logger.info("Lista de produtos vazia");
                logger.info("Fim do método buscarLista");
                return ResponseEntity.noContent().build();
            }
            logger.info("Lista de produtos encontrada: {}", produtos);
            logger.info("Fim do método buscarLista");
            return ResponseEntity.ok(produtos);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar lista de produtos", e);
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/produtos/{id}")
    public ResponseEntity<ProdutoDTO> buscarPorId(@PathVariable Long id){
        logger.info("Inicio do método buscarPorId");
        logger.info("Buscando produto por id: {}", id);
        try {
            ProdutoDTO produto = produtoService.buscarPorId(id);
            if (produto == null){
                logger.info("Produto não encontrado");
                logger.info("Fim do método buscarPorId");
                return ResponseEntity.noContent().build();
            }
            logger.info("Produto encontrado: {}", produto);
            logger.info("Fim do método buscarPorId");
            return ResponseEntity.ok(produto);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao buscar produto por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/produtos/{id}")
    @Transactional
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        logger.info("Inicio do método apagar");
        logger.info("Apagando produto por id: {}", id);
        try {
            produtoService.deletarProduto(id);
            logger.info("Produto apagado com sucesso");
            logger.info("Fim do método apagar");
            return ResponseEntity.noContent().build();
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao apagar produto por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/produtos/{id}")
    @Transactional
    public ResponseEntity<ProdutoDTO> atualizarProduto(@PathVariable Long id, @RequestBody ProdutoDTO produto) {
        logger.info("Inicio do método atualizarProduto");
        logger.info("Atualizando produto por id: {}", id);
        try {
            ProdutoDTO novoProduto = produtoService.atualizarProduto(id, produto);
            logger.info("Produto atualizado com sucesso: {}", novoProduto);
            logger.info("Fim do método atualizarProduto");
            return ResponseEntity.ok(novoProduto);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao atualizar unidade de medida por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/produtos")
    @Transactional
    public ResponseEntity<ProdutoDTO> cadastrarProduto(@RequestBody ProdutoDTO produto) {
        logger.info("Inicio do método cadastrarProduto");
        logger.info("Cadastrando produto: {}", produto);
        try {
            ProdutoDTO novoProduto = produtoService.cadastrarProduto(produto);
            logger.info("Produto cadastrado com sucesso: {}", novoProduto);
            logger.info("Fim do método cadastrarProduto");
            return ResponseEntity.ok(novoProduto);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao cadastrar produto", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
