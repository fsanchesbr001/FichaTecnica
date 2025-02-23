package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.dtos.ItensProdutoDTO;
import com.fabriciosanches.fichatecnica.domains.ItensProduto;
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


    private Optional<List<ItensProdutoDTO>> obterLista() {
        log.info("Inicio do método obterLista");
        Optional<List<ItensProdutoDTO>> listaRecord =
                Optional.of(ItensProdutoDTO.from(repository.findAll()));

        log.info("Lista de ItensProduto encontrada: {}", listaRecord.get());
        log.info("Fim do método obterLista");
        return listaRecord;
    }

    public List<ItensProdutoDTO> listar() {
        return obterLista().map(lista -> lista.stream()
                .toList()).orElseThrow(
                () -> new FichaTecnicaException("Lista de ItensProduto não encontrada"));

    }

    public ItensProdutoDTO buscarPorId(Long id) {
        return obterLista().map(lista -> lista.stream()
                        .filter(itemProd -> itemProd.codigo().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new FichaTecnicaException("Item Produto não encontrada")))
                .orElseThrow(() -> new FichaTecnicaException("Item Produto não encontrada"));
    }

    public ItensProdutoDTO atualizarItensProduto(Long id, ItensProdutoDTO novosDados) {
        Optional<ItensProduto> itensProdutoExistente = repository.findById(id);
        if (itensProdutoExistente.isPresent()) {
            ItensProduto itensProduto = itensProdutoExistente.get();
            itensProduto.setCodigo(novosDados.codigo());
            itensProduto.setQuantidade(novosDados.quantidade());
            itensProduto.setValor(novosDados.valor());
            itensProduto.setCdItem(novosDados.codigoItem());
            itensProduto.setCdProduto(novosDados.codProduto());

            // Atualize outros campos conforme necessário
            repository.save(itensProduto);
            return new ItensProdutoDTO(itensProduto);
        } else {
            throw new FichaTecnicaException("Item de Produto com ID " + id + " não encontrado");
        }
    }

    public ItensProdutoDTO cadastrar(ItensProdutoDTO itemProduto) {
        Objects.requireNonNull(itemProduto, "itemProduto não pode ser nula");
        Objects.requireNonNull(itemProduto.codProduto(), "Codigo do Produto não pode ser nulo");
        Objects.requireNonNull(itemProduto.quantidade(), "Quantidade não pode ser nula");
        Objects.requireNonNull(itemProduto.codigoItem(), "Codigo do Item não pode ser nulo");
        Objects.requireNonNull(itemProduto.valor(), "Valor não pode ser nulo");
        Objects.requireNonNull(itemProduto.unidadeMedida(), "Unidade não pode ser nula");


        ItensProduto novoItemProduto = new ItensProduto(itemProduto);
        return new ItensProdutoDTO(repository.save(novoItemProduto));
    }

    public void deletarItemProduto(Long id) {
        repository.deleteById(id);
    }
}
