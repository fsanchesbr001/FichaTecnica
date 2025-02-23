package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.dtos.UnidadeMedidaDTO;
import com.fabriciosanches.fichatecnica.domains.UnidadeMedida;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.UnidadeMedidaRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UnidadeMedidaService {
    private final Logger logger = LogManager.getLogger(UnidadeMedidaService.class);
    private final UnidadeMedidaRepository repository;

    public UnidadeMedidaService(UnidadeMedidaRepository repository) {
        this.repository = repository;
    }

    private Optional<List<UnidadeMedidaDTO>> obterLista() {
        logger.info("Inicio do método obterLista");
        Optional<List<UnidadeMedidaDTO>> listaRecord =
                Optional.of(UnidadeMedidaDTO.from(repository.findAll()));

        logger.info("Lista de unidades de medida encontrada: {}", listaRecord.get());
        logger.info("Fim do método obterLista");
        return listaRecord;
    }

    public List<UnidadeMedidaDTO> listar() {
       return obterLista().map(lista -> lista.stream()
                        .sorted(Comparator.comparing(UnidadeMedidaDTO::nome))
                        .toList()).orElseThrow(
                                () -> new FichaTecnicaException("Lista de unidades de medida não encontrada"));

    }

    public UnidadeMedidaDTO buscarPorId(Long id) {
        return obterLista().map(lista -> lista.stream()
                .filter(unidade -> unidade.codigo().equals(id))
                .findFirst()
                .orElseThrow(() -> new FichaTecnicaException("Unidade de medida não encontrada")))
                .orElseThrow(() -> new FichaTecnicaException("Lista de unidades de medida não encontrada"));
    }

    public long findByName(String nome) {
        return repository.countByName(nome);
    }

    public UnidadeMedidaDTO atualizarUnidade(Long id, UnidadeMedidaDTO novosDados) {
        Optional<UnidadeMedida> unidadeExistente = repository.findById(id);
        if (unidadeExistente.isPresent()) {
            UnidadeMedida unidade = unidadeExistente.get();
            unidade.setNome(novosDados.nome());
            unidade.setSigla(novosDados.sigla());

            // Atualize outros campos conforme necessário
            repository.save(unidade);
            return new UnidadeMedidaDTO(unidade);
        } else {
            throw new FichaTecnicaException("Unidade com ID " + id + " não encontrada");
        }
    }

    public UnidadeMedidaDTO cadastrarUnidade(UnidadeMedidaDTO unidade) {
        Objects.requireNonNull(unidade, "Unidade de medida não pode ser nula");
        Objects.requireNonNull(unidade.nome(), "Nome da unidade de medida não pode ser nulo");
        Objects.requireNonNull(unidade.sigla(), "Sigla da unidade de medida não pode ser nula");

        if (findByName(unidade.nome()) > 0) {
            throw new FichaTecnicaException("Unidade de medida já cadastrada");
        }

        UnidadeMedida unidadeMedida = new UnidadeMedida(unidade);
        return new UnidadeMedidaDTO(repository.save(unidadeMedida));
    }

    public void deletarUnidade(Long id) {
        repository.deleteById(id);
    }
}
