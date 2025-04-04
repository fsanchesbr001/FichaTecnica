package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.HistoricoItem;
import com.fabriciosanches.fichatecnica.dtos.ItemDTO;
import com.fabriciosanches.fichatecnica.domains.Item;
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

    private Optional<List<ItemDTO>> obterLista() {
        logger.info("Inicio do método obterLista");
        Optional<List<ItemDTO>> listaRecord =
                Optional.of(ItemDTO.from(repository.findAll()));

        logger.info("Lista de itens encontrada: {}", listaRecord.get());
        logger.info("Fim do método obterLista");
        return listaRecord;
    }

    public List<ItemDTO> listar() {
        return obterLista().map(lista -> lista.stream()
                .sorted(Comparator.comparing(ItemDTO::nome))
                .toList()).orElseThrow(
                () -> new FichaTecnicaException("Lista de itens não encontrada"));

    }

    public ItemDTO buscarPorId(Long id) {
        return repository.findAll().stream()
                .map(ItemDTO::new)
                .filter(item -> id.equals(item.codigo()))
                .findFirst()
                .orElseThrow(() -> new FichaTecnicaException("Item com ID " + id + " não encontrado"));
    }

    public long findByName(String nome) {
        return repository.countByName(nome);
    }

    @Modifying
    @Transactional
    public ItemDTO atualizarItem(Long id, ItemDTO novosDados) {
        Optional<Item> itemExistente = repository.findById(id);
        if (itemExistente.isPresent()) {
            Item item = itemExistente.get();
            item.setNome(novosDados.nome());
            item.setValor(novosDados.valor());
            item.setUnidadeMedida(novosDados.unidadeMedida());

            // Atualize outros campos conforme necessário

            ItemDTO itemDTO = new ItemDTO(repository.save(item));

            HistoricoItem historicoItem = new HistoricoItem(null,itemDTO.codigo() , novosDados.valor(), LocalDate.now());

            historicoItemRepository.save(historicoItem);

            return itemDTO;
        } else {
            throw new FichaTecnicaException("Item com ID " + id + " não encontrado");
        }
    }

    @Modifying
    @Transactional
    public ItemDTO cadastrarItem(ItemDTO item) {
        Objects.requireNonNull(item, "Item não pode ser nulo");
        Objects.requireNonNull(item.nome(), "Nome do item não pode ser nulo");
        Objects.requireNonNull(item.unidadeMedida(), "Unidade de medida não pode ser nula");
        Objects.requireNonNull(item.valor(), "Valor do item não pode ser nulo");

        if (findByName(item.nome()) > 0) {
            throw new FichaTecnicaException("Item já cadastrado");
        }

        Item novoItem = new Item(item);

        ItemDTO itemDTO = new ItemDTO(repository.save(novoItem));

        HistoricoItem historicoItem = new HistoricoItem(null, itemDTO.codigo(), item.valor(), LocalDate.now());

        historicoItemRepository.save(historicoItem);

        return itemDTO;
    }

    @Modifying
    public void deletarItem(Long id) {
        if(!historicoItemRepository.findByCdItem(id).isEmpty()) {
            throw new FichaTecnicaException("Existem históricos para o item " + id);
        }
        repository.deleteById(id);
    }
}
