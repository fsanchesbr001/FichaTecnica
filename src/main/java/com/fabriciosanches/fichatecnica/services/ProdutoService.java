package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fabriciosanches.fichatecnica.domains.Produto;
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

    public ProdutoDTO atualizarProduto(Long id, ProdutoDTO novosDados) {
        Optional<Produto> produtoExistente = repository.findById(id);
        if (produtoExistente.isPresent()) {
            Produto produto = produtoExistente.get();
            produto.setNome(novosDados.nome());
            produto.setDescricao(novosDados.descricao());
            produto.setImagem(novosDados.imagem());
            produto.setValor(novosDados.valor());

            // Atualize outros campos conforme necessário
            repository.save(produto);
            return new ProdutoDTO(produto);
        } else {
            throw new FichaTecnicaException("Produto com ID " + id + " não encontrada");
        }
    }

    public ProdutoDTO cadastrarProduto(ProdutoDTO produto) {
        Objects.requireNonNull(produto, "Produto não pode ser nulo");
        Objects.requireNonNull(produto.nome(), "Nome do produto não pode ser nulo");
        Objects.requireNonNull(produto.descricao(), "Descricao não pode ser nula");
        Objects.requireNonNull(produto.valor(), "Valor não pode ser nulo");

        if (findByName(produto.nome()) > 0) {
            throw new FichaTecnicaException("Produto já cadastrado");
        }

        Produto novoProduto = new Produto(produto);
        return new ProdutoDTO(repository.save(novoProduto));
    }

    public void deletarProduto(Long id) {
        repository.deleteById(id);
    }
}
