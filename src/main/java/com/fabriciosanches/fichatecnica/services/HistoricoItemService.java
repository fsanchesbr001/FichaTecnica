package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.HistoricoItem;
import com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.HistoricoItemRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class HistoricoItemService {

    private final Logger logger = LogManager.getLogger(HistoricoItemService.class);
    private final HistoricoItemRepository repository;

    public HistoricoItemService(HistoricoItemRepository repository) {
        this.repository = repository;
    }

    private Optional<List<HistoricoItemDTO>> obterLista() {
        logger.info("Inicio do método obterLista");
        Optional<List<HistoricoItemDTO>> listaRecord =
                Optional.of(com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO.from(repository.findAll()));

        logger.info("Lista de historico de itens encontrada: {}", listaRecord.get());
        logger.info("Fim do método obterLista");
        return listaRecord;
    }

    public List<HistoricoItemDTO> listar() {
        return obterLista().map(lista -> lista.stream()
                .toList()).orElseThrow(
                () -> new FichaTecnicaException("Lista de historico de itens não encontrada"));

    }

    public HistoricoItemDTO buscarPorId(Long id) {
        return obterLista().map(lista -> lista.stream()
                        .filter(item -> item.codigo().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new FichaTecnicaException("Historico de item não encontrado")))
                .orElseThrow(() -> new FichaTecnicaException("Lista de historico de itens não encontrada"));
    }


    public HistoricoItemDTO atualizarHistoricoItem(Long id, HistoricoItemDTO novosDados) {
        Optional<HistoricoItem> historicoExistente = repository.findById(id);
        if (historicoExistente.isPresent()) {
            HistoricoItem historicoItem = historicoExistente.get();
            historicoItem.setItem(novosDados.item());
            historicoItem.setValor(novosDados.valor());
            historicoItem.setDataInicio(novosDados.dataInicio());

            // Atualize outros campos conforme necessário
            repository.save(historicoItem);
            return new HistoricoItemDTO(historicoItem);
        } else {
            throw new FichaTecnicaException("Historico de item com ID " + id + " não encontrado");
        }
    }

    public HistoricoItemDTO cadastrarItem(HistoricoItemDTO historicoItem) {
        Objects.requireNonNull(historicoItem, "Historico de item não pode ser nulo");
        Objects.requireNonNull(historicoItem.item(), "Item não pode ser nulo");
        Objects.requireNonNull(historicoItem.dataInicio(), "Data Inicio não pode ser nula");
        Objects.requireNonNull(historicoItem.valor(), "Valor do item não pode ser nulo");

        HistoricoItem novoHistoricoItem = new HistoricoItem(historicoItem);
        return new HistoricoItemDTO(repository.save(novoHistoricoItem));
    }

    public void deletarItem(Long id) {
        repository.deleteById(id);
    }

    public void deletarPorCodigoItem(Long codItem) {
        repository.deleteByItemCodigo(codItem);
    }

    public List<HistoricoItemDTO> buscarPorCodigoItem(Long codItem) {
        return com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO.from(repository.findByItemCodigo(codItem));
    }
}
