package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutoDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ProdutoUseCase implements CriarProdutoPort, BuscarProdutoPort, AtualizarProdutoPort, DeletarProdutoPort {

    private final ProdutoRepositoryPort produtoRepositoryPort;

    public ProdutoUseCase(ProdutoRepositoryPort produtoRepositoryPort) {
        this.produtoRepositoryPort = Objects.requireNonNull(produtoRepositoryPort, "ProdutoRepositoryPort nÃ£o pode ser nulo");
    }

    @Override
    public List<ProdutoDTO> listar() {
        return produtoRepositoryPort.buscarTodos().stream()
                .sorted(Comparator.comparing(com.fabriciosanches.fichatecnica.core.domain.Produto::getNome))
                .map(this::toDto)
                .toList();
    }

    @Override
    public ProdutoDTO buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id nÃ£o pode ser nulo");
        }

        Produto produto = produtoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new FichaTecnicaException("Produto nÃ£o encontrada"));
        return toDto(produto);
    }

    public long findByName(String nome) {
        return produtoRepositoryPort.contarPorNome(nome);
    }

    @Override
    public ProdutoDTO cadastrarProduto(ProdutoDTO produtoDTO) {
        Objects.requireNonNull(produtoDTO, "Produto nÃ£o pode ser nulo");
        Objects.requireNonNull(produtoDTO.nome(), "Nome do produto nÃ£o pode ser nulo");
        Objects.requireNonNull(produtoDTO.descricao(), "Descricao nÃ£o pode ser nula");
        Objects.requireNonNull(produtoDTO.valorVenda(), "Valor de Venda nÃ£o pode ser nulo");

        if (findByName(produtoDTO.nome()) > 0) {
            throw new FichaTecnicaException("Produto jÃ¡ cadastrado");
        }

        Produto produto = new Produto(
                produtoDTO.codigo(),
                produtoDTO.nome(),
                produtoDTO.descricao(),
                produtoDTO.imagem(),
                produtoDTO.valorVenda(),
                produtoDTO.valorItens(),
                List.of()
        );

        return toDto(produtoRepositoryPort.salvar(produto));
    }

    @Override
    public ProdutoDTO atualizarProduto(Long id, ProdutoDTO novosDados) {
        if (id == null) {
            throw new IllegalArgumentException("Id nÃ£o pode ser nulo");
        }
        Objects.requireNonNull(novosDados, "Produto nÃ£o pode ser nulo");

        Produto produto = produtoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new FichaTecnicaException("Produto com ID " + id + " nÃ£o encontrada"));

        produto.setNome(novosDados.nome());
        produto.setDescricao(novosDados.descricao());
        produto.setImagem(novosDados.imagem());
        produto.setValorVenda(novosDados.valorVenda());
        produto.setValorItens(novosDados.valorItens());

        return toDto(produtoRepositoryPort.salvar(produto));
    }

    @Override
    public void deletar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id nÃ£o pode ser nulo");
        }
        produtoRepositoryPort.deletarPorId(id);
    }

    private ProdutoDTO toDto(com.fabriciosanches.fichatecnica.core.domain.Produto produto) {
        return new ProdutoDTO(
                produto.getCodigo(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getImagem(),
                produto.getValorVenda(),
                produto.getValorItens());
    }
}


