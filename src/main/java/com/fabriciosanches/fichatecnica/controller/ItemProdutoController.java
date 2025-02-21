package com.fabriciosanches.fichatecnica.controller;

import com.fabriciosanches.fichatecnica.domain.itensProduto.DadosItensProduto;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.ItensProdutoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("ficha-tecnica")
public class ItemProdutoController {
    private static final Logger logger = LogManager.getLogger(ItemProdutoController.class);

    final ItensProdutoService itensProdutoService;

    @GetMapping("/itens-produto")
    public ResponseEntity<List<DadosItensProduto>> buscarLista(){
        log.info("Inicio do método buscarLista");
        log.info("Buscando lista de itens de produto");
        try {
            List<DadosItensProduto> itensProdutos = itensProdutoService.listar();
            log.info("Lista de itens de produtos encontrada: {}", itensProdutos);
            log.info("Fim do método buscarLista");
            return ResponseEntity.ok(itensProdutos);
        }
        catch (FichaTecnicaException e){
            log.error("Erro ao buscar lista de itens de produto", e);
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/itens-produto/{id}")
    public ResponseEntity<DadosItensProduto> buscarPorId(@PathVariable Long id){
        log.info("Inicio do método buscarPorId");
        log.info("Buscando itens de produto por id: {}", id);
        try {
            DadosItensProduto dadosItensProduto = itensProdutoService.buscarPorId(id);
            log.info("Item de produto encontrado: {}", dadosItensProduto);
            log.info("Fim do método buscarPorId");
            return ResponseEntity.ok(dadosItensProduto);
        }
        catch (FichaTecnicaException e){
            log.error("Erro ao buscar itens de produto por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/itens-produto/{id}")
    @Transactional
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        log.info("Inicio do método apagar");
        log.info("Apagando itens de produto por id: {}", id);
        try {
            itensProdutoService.deletarItemProduto(id);
            log.info("Item de Produto apagado com sucesso");
            log.info("Fim do método apagar");
            return ResponseEntity.noContent().build();
        }
        catch (FichaTecnicaException e){
            log.error("Erro ao apagar item de produto por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/itens-produto/{id}")
    @Transactional
    public ResponseEntity<DadosItensProduto> atualizar(@PathVariable Long id, @RequestBody DadosItensProduto itensProduto) {
        log.info("Inicio do método atualizar");
        log.info("Atualizando itens de produto por id: {}", id);
        try {
            DadosItensProduto itemProduto = itensProdutoService.atualizarItensProduto(id, itensProduto);
            log.info("Item de produto atualizado com sucesso: {}", itemProduto);
            log.info("Fim do método atualizar");
            return ResponseEntity.ok(itemProduto);
        }
        catch (FichaTecnicaException e){
            log.error("Erro ao atualizar item de produto por id", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/itens-produto")
    @Transactional
    public ResponseEntity<DadosItensProduto> cadastrar(@RequestBody DadosItensProduto item) {
        log.info("Inicio do método cadastrar");
        log.info("Cadastrando item de produto: {}", item);
        try {
            DadosItensProduto itemAtual = itensProdutoService.cadastrar(item);
            log.info("Item de Produto cadastrado com sucesso: {}",itemAtual );
            log.info("Fim do método cadastrar");
            return ResponseEntity.ok(itemAtual);
        }
        catch (FichaTecnicaException e){
            logger.error("Erro ao cadastrar item de Produto", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
