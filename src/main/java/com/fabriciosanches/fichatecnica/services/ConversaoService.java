package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.Conversao;
import com.fabriciosanches.fichatecnica.dtos.ConversaoDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ConversaoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ConversaoService {
    private final Logger logger = LogManager.getLogger(ConversaoService.class);
    private final ConversaoRepository repository;

    public ConversaoService(ConversaoRepository repository) {
        this.repository = repository;
    }

    private Optional<List<ConversaoDTO>> obterLista() {
        logger.info("Inicio do método obterLista");
        Optional<List<ConversaoDTO>> listaRecord =
                Optional.of(ConversaoDTO.from(repository.findAll()));

        logger.info("Lista de conversão encontrada: {}", listaRecord.get());
        logger.info("Fim do método obterLista");
        return listaRecord;
    }

    public List<ConversaoDTO> listar() {
       return obterLista().map(lista -> lista.stream()
                        .sorted(Comparator.comparing(ConversaoDTO::unidadeDe))
                        .toList()).orElseThrow(
                                () -> new FichaTecnicaException("Lista de conversões não encontrada"));

    }

    public ConversaoDTO buscarPorId(Long id) {
        return repository.findAll().stream()
                .map(ConversaoDTO::new)
                .filter(conversao -> id.equals(conversao.codigo()))
                .findFirst()
                .orElseThrow(() -> new FichaTecnicaException("Conversão com ID " + id + " não encontrada"));
    }

    public long findByConversaoDePara(Long conversaoDe, Long conversaoPara) {
        return repository.countByUnidadeDeAndUnidadePara(conversaoDe, conversaoPara);
    }

    public ConversaoDTO atualizarConversao(Long id, ConversaoDTO novosDados) {
        Optional<Conversao> conversaoExistente = repository.findById(id);
        if (conversaoExistente.isPresent()) {
            Conversao conversao = conversaoExistente.get();
            conversao.setUnidadeDe(novosDados.unidadeDe());
            conversao.setUnidadePara(novosDados.unidadePara());
            conversao.setOperacao(novosDados.operacao());
            conversao.setValor(novosDados.valor());

            // Atualize outros campos conforme necessário
            repository.save(conversao);
            return new ConversaoDTO(conversao);
        } else {
            throw new FichaTecnicaException("Conversao com ID " + id + " não encontrada");
        }
    }

    public ConversaoDTO cadastrarConversao(ConversaoDTO conversao) {
        Objects.requireNonNull(conversao, "Conversão não pode ser nula");
        Objects.requireNonNull(conversao.unidadeDe(), "UnidadeDe não pode ser nulo");
        Objects.requireNonNull(conversao.operacao(), "Operação não pode ser nula");
        Objects.requireNonNull(conversao.valor(), "Valor não pode ser nulo");


        if (findByConversaoDePara(conversao.unidadeDe(), conversao.unidadePara()) > 0) {
            throw new FichaTecnicaException("Conversão já cadastrada");
        }

        Conversao novaConversao = new Conversao(conversao);
        return new ConversaoDTO(repository.save(novaConversao));
    }

    public void deletarConversao(Long id) {
        repository.deleteById(id);
    }
}
