package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.HistoricoItemRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<HistoricoItemDTO> buscarPorCodigoItem(Long codItem) {
        return HistoricoItemDTO.from(repository.findByCdItem(codItem));
    }
}
