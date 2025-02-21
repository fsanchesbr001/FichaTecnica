package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domain.produto.DadosProduto;
import com.fabriciosanches.fichatecnica.domain.produto.Produto;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

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

    private Optional<List<DadosProduto>> obterLista() {
        logger.info("Inicio do método obterLista");
        Optional<List<DadosProduto>> listaRecord =
                Optional.of(DadosProduto.from(repository.findAll()));

        logger.info("Lista de produtos encontrada: {}", listaRecord.get());
        logger.info("Fim do método obterLista");
        return listaRecord;
    }

    public List<DadosProduto> listar() {
       return obterLista().map(lista -> lista.stream()
                        .sorted(Comparator.comparing(DadosProduto::nome))
                        .toList()).orElseThrow(
                                () -> new FichaTecnicaException("Lista de produtos não encontrada"));

    }

    public DadosProduto buscarPorId(Long id) {
        return obterLista().map(lista -> lista.stream()
                .filter(produto -> produto.codigo().equals(id))
                .findFirst()
                .orElseThrow(() -> new FichaTecnicaException("Produto não encontrada")))
                .orElseThrow(() -> new FichaTecnicaException("Lista de produtos não encontrada"));
    }

    public long findByName(String nome) {
        return repository.countByName(nome);
    }

    public DadosProduto atualizarProduto(Long id, DadosProduto novosDados) {
        Optional<Produto> produtoExistente = repository.findById(id);
        if (produtoExistente.isPresent()) {
            Produto produto = produtoExistente.get();
            produto.setNome(novosDados.nome());
            produto.setDescricao(novosDados.descricao());
            produto.setImagem(novosDados.imagem());
            produto.setValor(novosDados.valor());
            produto.setItens(novosDados.itens());

            // Atualize outros campos conforme necessário
            repository.save(produto);
            return new DadosProduto(produto);
        } else {
            throw new FichaTecnicaException("Produto com ID " + id + " não encontrada");
        }
    }

    public DadosProduto cadastrarProduto(DadosProduto produto) {
        Objects.requireNonNull(produto, "Produto não pode ser nulo");
        Objects.requireNonNull(produto.nome(), "Nome do produto não pode ser nulo");
        Objects.requireNonNull(produto.descricao(), "Descricao não pode ser nula");
        Objects.requireNonNull(produto.valor(), "Valor não pode ser nulo");
        Objects.requireNonNull(produto.itens(), "Itens não podem ser nulos");

        if (findByName(produto.nome()) > 0) {
            throw new FichaTecnicaException("Produto já cadastrado");
        }

        Produto novoProduto = new Produto(produto);
        return new DadosProduto(repository.save(novoProduto));
    }

    public void deletarProduto(Long id) {
        repository.deleteById(id);
    }
}
