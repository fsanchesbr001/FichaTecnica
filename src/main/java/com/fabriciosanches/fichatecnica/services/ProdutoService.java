package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fabriciosanches.fichatecnica.domains.Produto;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProdutoService {
    private final Logger logger = LogManager.getLogger(ProdutoService.class);
    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    private Optional<List<ProdutoDTO>> obterLista() {
        logger.info("Inicio do método obterLista");
        Optional<List<ProdutoDTO>> listaRecord =
                Optional.of(ProdutoDTO.from(repository.findAll()));

        logger.info("Lista de produtos encontrada: {}", listaRecord.get());
        logger.info("Fim do método obterLista");
        return listaRecord;
    }

    public List<ProdutoDTO> listar() {
       return obterLista().map(lista -> lista.stream()
                        .sorted(Comparator.comparing(ProdutoDTO::nome))
                        .toList()).orElseThrow(
                                () -> new FichaTecnicaException("Lista de produtos não encontrada"));

    }

    public ProdutoDTO buscarPorId(Long id) {
        return obterLista().map(lista -> lista.stream()
                .filter(produto -> produto.codigo().equals(id))
                .findFirst()
                .orElseThrow(() -> new FichaTecnicaException("Produto não encontrada")))
                .orElseThrow(() -> new FichaTecnicaException("Lista de produtos não encontrada"));
    }

    public long findByName(String nome) {
        return repository.countByName(nome);
    }

    @Transactional
    public ProdutoDTO atualizarProduto(Long id, ProdutoDTO novosDados) {
        Optional<Produto> produtoExistente = repository.findById(id);
        if (produtoExistente.isPresent()) {
            Produto produto = produtoExistente.get();
            produto.setNome(novosDados.nome());
            produto.setDescricao(novosDados.descricao());
            produto.setImagem(novosDados.imagem());
            produto.setValorVenda(novosDados.valorVenda());
            produto.setValorItens(novosDados.valorItens());
            produto.setItens(novosDados.itensProduto());

            // Atualize outros campos conforme necessário
            repository.save(produto);
            return new ProdutoDTO(produto);
        } else {
            throw new FichaTecnicaException("Produto com ID " + id + " não encontrada");
        }
    }

    @Transactional
    public ProdutoDTO cadastrarProduto(ProdutoDTO produtoDTO) {
        Objects.requireNonNull(produtoDTO, "Produto não pode ser nulo");
        Objects.requireNonNull(produtoDTO.nome(), "Nome do produto não pode ser nulo");
        Objects.requireNonNull(produtoDTO.descricao(), "Descricao não pode ser nula");
        Objects.requireNonNull(produtoDTO.valorVenda(), "Valor de Venda não pode ser nulo");
        Objects.requireNonNull(produtoDTO.valorItens(), "Valor dos Itens não pode ser nulo");
        Objects.requireNonNull(produtoDTO.itensProduto(), "Itens do Produto não podem ser nulos");

        if (findByName(produtoDTO.nome()) > 0) {
            throw new FichaTecnicaException("Produto já cadastrado");
        }

        Produto produto = new Produto(produtoDTO);
        Produto produtoSalvo = repository.save(produto);

        produtoDTO.itensProduto().forEach(item -> item.setProduto(produtoSalvo));
        produtoSalvo.setItens(produtoDTO.itensProduto());

        return new ProdutoDTO(repository.save(produtoSalvo));

    }

    @Transactional
    public void deletarProduto(Long id) {
        repository.deleteById(id);
    }
}
