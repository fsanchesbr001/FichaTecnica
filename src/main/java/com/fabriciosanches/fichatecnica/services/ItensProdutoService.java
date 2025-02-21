package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domain.itensProduto.DadosItensProduto;
import com.fabriciosanches.fichatecnica.domain.itensProduto.ItensProduto;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ItensProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItensProdutoService {

    private final ItensProdutoRepository repository;


    private Optional<List<DadosItensProduto>> obterLista() {
        log.info("Inicio do método obterLista");
        Optional<List<DadosItensProduto>> listaRecord =
                Optional.of(DadosItensProduto.from(repository.findAll()));

        log.info("Lista de ItensProduto encontrada: {}", listaRecord.get());
        log.info("Fim do método obterLista");
        return listaRecord;
    }

    public List<DadosItensProduto> listar() {
        return obterLista().map(lista -> lista.stream()
                .toList()).orElseThrow(
                () -> new FichaTecnicaException("Lista de ItensProduto não encontrada"));

    }

    public DadosItensProduto buscarPorId(Long id) {
        return obterLista().map(lista -> lista.stream()
                        .filter(itemProd -> itemProd.codigo().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new FichaTecnicaException("Item Produto não encontrada")))
                .orElseThrow(() -> new FichaTecnicaException("Item Produto não encontrada"));
    }

    public DadosItensProduto atualizarItensProduto(Long id, DadosItensProduto novosDados) {
        Optional<ItensProduto> itensProdutoExistente = repository.findById(id);
        if (itensProdutoExistente.isPresent()) {
            ItensProduto itensProduto = itensProdutoExistente.get();
            itensProduto.setCodigo(novosDados.codigo());
            itensProduto.setProduto(novosDados.produto());
            itensProduto.setQuantidade(novosDados.quantidade());
            itensProduto.setValor(novosDados.valor());

            // Atualize outros campos conforme necessário
            repository.save(itensProduto);
            return new DadosItensProduto(itensProduto);
        } else {
            throw new FichaTecnicaException("Item de Produto com ID " + id + " não encontrado");
        }
    }

    public DadosItensProduto cadastrar(DadosItensProduto itemProduto) {
        Objects.requireNonNull(itemProduto, "itemProduto não pode ser nula");
        Objects.requireNonNull(itemProduto.produto(), "Produto não pode ser nulo");
        Objects.requireNonNull(itemProduto.codProduto(), "Codigo do Produto não pode ser nulo");
        Objects.requireNonNull(itemProduto.quantidade(), "Quantidade não pode ser nula");
        Objects.requireNonNull(itemProduto.codigoItem(), "Codigo do Item não pode ser nulo");
        Objects.requireNonNull(itemProduto.valor(), "Valor não pode ser nulo");
        Objects.requireNonNull(itemProduto.unidadeMedida(), "Unidade não pode ser nula");


        ItensProduto novoItemProduto = new ItensProduto(itemProduto);
        return new DadosItensProduto(repository.save(novoItemProduto));
    }

    public void deletarItemProduto(Long id) {
        repository.deleteById(id);
    }
}
