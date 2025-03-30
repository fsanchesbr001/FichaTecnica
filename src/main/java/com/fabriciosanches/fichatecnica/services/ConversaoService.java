package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.Conversao;
import com.fabriciosanches.fichatecnica.dtos.ConversaoDTO;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ConversaoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ConversaoService {
    private final Logger logger = LogManager.getLogger(ConversaoService.class);
    private final ConversaoRepository repository;
    private final ItemService itemService;

    public ConversaoService(ConversaoRepository repository, ItemService itemService) {
        this.repository = repository;
        this.itemService = itemService;
    }

    private Optional<List<ConversaoDTO>> obterLista() {
        logger.info("Inicio do método obterLista");
        var listaRecord = Optional.of(ConversaoDTO.from(repository.findAll()));
        logger.info("Lista de conversão encontrada: {}", listaRecord.get());
        logger.info("Fim do método obterLista");
        return listaRecord;
    }

    public List<ConversaoDTO> listar() {
        return obterLista()
                .map(lista -> lista.stream()
                        .sorted(Comparator.comparing(ConversaoDTO::unidadeDe))
                        .toList())
                .orElseThrow(() -> new FichaTecnicaException("Lista de conversões não encontrada"));
    }

    public ConversaoDTO buscarPorId(Long id) {
        return repository.findById(id)
                .map(ConversaoDTO::new)
                .orElseThrow(() -> new FichaTecnicaException("Conversão com ID " + id + " não encontrada"));
    }

    public long findByConversaoDePara(Long conversaoDe, Long conversaoPara) {
        return repository.countByUnidadeDeAndUnidadePara(conversaoDe, conversaoPara);
    }

    public ConversaoDTO atualizarConversao(Long id, ConversaoDTO novosDados) {
        var conversaoExistente = repository.findById(id)
                .orElseThrow(() -> new FichaTecnicaException("Conversão com ID " + id + " não encontrada"));

        conversaoExistente.setUnidadeDe(novosDados.unidadeDe());
        conversaoExistente.setUnidadePara(novosDados.unidadePara());
        conversaoExistente.setOperacao(novosDados.operacao());
        conversaoExistente.setValor(novosDados.valor());

        repository.save(conversaoExistente);
        return new ConversaoDTO(conversaoExistente);
    }

    public ConversaoDTO cadastrarConversao(ConversaoDTO conversao) {
        Objects.requireNonNull(conversao, "Conversão não pode ser nula");
        Objects.requireNonNull(conversao.unidadeDe(), "UnidadeDe não pode ser nulo");
        Objects.requireNonNull(conversao.operacao(), "Operação não pode ser nula");
        Objects.requireNonNull(conversao.valor(), "Valor não pode ser nulo");

        if (findByConversaoDePara(conversao.unidadeDe(), conversao.unidadePara()) > 0) {
            throw new FichaTecnicaException("Conversão já cadastrada");
        }

        var novaConversao = new Conversao(conversao);
        return new ConversaoDTO(repository.save(novaConversao));
    }

    public void deletarConversao(Long id) {
        repository.deleteById(id);
    }

    public ConversaoValoresDTO obterValoresConversao(Long idItem, Double quantidade, Long idUnidade) {
        logger.info("Iniciando o método obterValoresConversao com idItem: {}, quantidade: {}, idUnidade: {}", idItem, quantidade, idUnidade);
        var itemDto = itemService.buscarPorId(idItem);

        var idUnidadeMedidaCompra = itemDto.unidadeMedida().getCodigo();
        var valorCompra = itemDto.valor();

        var conversao = repository.findByUnidadeDeAndUnidadePara(idUnidadeMedidaCompra,
                idUnidade);

        return converterValores(conversao, valorCompra, quantidade);
    }

    private ConversaoValoresDTO converterValores(Conversao conversao, BigDecimal valorCompra, Double quantidade) {
        logger.info("Iniciando o método converterValores com conversao: {}", conversao);
        var valorConvertido = switch (conversao.getOperacao()) {
            case "MULTIPLICA" -> valorCompra.multiply(conversao.getValor()).multiply(BigDecimal.valueOf(quantidade));
            case "DIVIDE" -> valorCompra.divide(conversao.getValor()).multiply(BigDecimal.valueOf(quantidade));
            default -> throw new FichaTecnicaException("Operação inválida");
        };

        var resultado = new ConversaoValoresDTO(quantidade, conversao.getUnidadePara(), valorConvertido);
        logger.info("Resultado da conversão: {}", resultado);
        return resultado;
    }
}
