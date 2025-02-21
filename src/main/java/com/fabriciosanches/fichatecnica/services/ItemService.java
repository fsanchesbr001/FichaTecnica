package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domain.historicoItem.HistoricoItem;
import com.fabriciosanches.fichatecnica.domain.itens.DadosItem;
import com.fabriciosanches.fichatecnica.domain.itens.Item;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.HistoricoItemRepository;
import com.fabriciosanches.fichatecnica.repository.ItemRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ItemService {

    private final Logger logger = LogManager.getLogger(ItemService.class);
    private final ItemRepository repository;
    private final HistoricoItemRepository historicoItemRepository;



    public ItemService(ItemRepository repository, HistoricoItemRepository historicoItemRepository) {
        this.repository = repository;
        this.historicoItemRepository = historicoItemRepository;
    }

    private Optional<List<DadosItem>> obterLista() {
        logger.info("Inicio do método obterLista");
        Optional<List<DadosItem>> listaRecord =
                Optional.of(DadosItem.from(repository.findAll()));

        logger.info("Lista de itens encontrada: {}", listaRecord.get());
        logger.info("Fim do método obterLista");
        return listaRecord;
    }

    public List<DadosItem> listar() {
        return obterLista().map(lista -> lista.stream()
                .sorted(Comparator.comparing(DadosItem::nome))
                .toList()).orElseThrow(
                () -> new FichaTecnicaException("Lista de itens não encontrada"));

    }

    public DadosItem buscarPorId(Long id) {
        return obterLista().map(lista -> lista.stream()
                        .filter(item -> item.codigo().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new FichaTecnicaException("Item não encontrado")))
                .orElseThrow(() -> new FichaTecnicaException("Lista de itens não encontrada"));
    }

    public long findByName(String nome) {
        return repository.countByName(nome);
    }

    @Modifying
    @Transactional
    public DadosItem atualizarItem(Long id, DadosItem novosDados) {
        Optional<Item> itemExistente = repository.findById(id);
        if (itemExistente.isPresent()) {
            Item item = itemExistente.get();
            item.setNome(novosDados.nome());
            item.setValor(novosDados.valor());
            item.setUnidadeMedida(novosDados.unidadeMedida());

            // Atualize outros campos conforme necessário

            DadosItem dadosItem = new DadosItem(repository.save(item));

            HistoricoItem historicoItem = new HistoricoItem(null, item, novosDados.valor(), LocalDate.now());

            historicoItemRepository.save(historicoItem);

            return dadosItem;
        } else {
            throw new FichaTecnicaException("Item com ID " + id + " não encontrado");
        }
    }

    @Modifying
    @Transactional
    public DadosItem cadastrarItem(DadosItem item) {
        Objects.requireNonNull(item, "Item não pode ser nulo");
        Objects.requireNonNull(item.nome(), "Nome do item não pode ser nulo");
        Objects.requireNonNull(item.unidadeMedida(), "Unidade de medida não pode ser nula");
        Objects.requireNonNull(item.valor(), "Valor do item não pode ser nulo");

        if (findByName(item.nome()) > 0) {
            throw new FichaTecnicaException("Item já cadastrado");
        }

        Item novoItem = new Item(item);

        DadosItem dadosItem = new DadosItem(repository.save(novoItem));

        HistoricoItem historicoItem = new HistoricoItem(null, novoItem, item.valor(), LocalDate.now());

        historicoItemRepository.save(historicoItem);

        return dadosItem;
    }

    @Modifying
    public void deletarItem(Long id) {
        historicoItemRepository.deleteByItemCodigo(id);
        repository.deleteById(id);
    }
}
