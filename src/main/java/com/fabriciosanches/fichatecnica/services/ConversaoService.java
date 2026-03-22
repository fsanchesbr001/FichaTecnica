package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.Conversao;
import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.dtos.ConversaoDTO;
import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.dtos.ItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ConversaoRepository;
import jakarta.transaction.Transactional;
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

    public ConversaoService(ConversaoRepository repository) {
        this.repository = repository;
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

    /**
     * Retorna todas as conversões com os nomes das unidades de medida resolvidos via JPQL JOIN.
     * Usado exclusivamente na geração de relatórios PDF.
     */
    public List<ConversaoRelatorioDTO> listarParaPdf() {
        return repository.findAllComNomes();
    }

    /**
     * Retorna uma conversão com os nomes das unidades de medida resolvidos via JPQL JOIN.
     * Usado exclusivamente na geração de relatórios PDF.
     */
    public ConversaoRelatorioDTO buscarPorIdParaPdf(Long id) {
        return repository.findByIdComNomes(id)
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

    @Transactional
    public void deletarConversao(Long id) {
        repository.deleteById(id);
    }

    public ConversaoValoresDTO obterValoresConversao(Item item, Double quantidade, Long idUnidade) {
        logger.info("Iniciando o método obterValoresConversao com idItem: {}, quantidade: {}, idUnidade: {}",
                item.getCodigo(), quantidade, idUnidade);
        var valoresConversaoValidos = validaValoresConversao(item.getCodigo(), quantidade, idUnidade);
        if (!valoresConversaoValidos) {
            logger.error("Valores de conversão inválidos");
            throw new FichaTecnicaException("Valores de conversão inválidos");
        }
        logger.info("Valores de conversão válidos");

        var itemDto = new ItemDTO(item);

        var idUnidadeMedidaCompra = itemDto.unidadeMedida().getCodigo();
        var valorCompra = itemDto.valor();

        var conversao = repository.findByUnidadeDeAndUnidadePara(idUnidadeMedidaCompra,
                idUnidade);

        return converterValores(conversao, valorCompra, quantidade);
    }

    private Boolean validaValoresConversao(Long idItem, Double quantidade, Long idUnidade) {
        logger.info("Iniciando o método validaValoresConversao com idItem: {}, quantidade: {}, idUnidade: {}",
                idItem, quantidade, idUnidade);
        if (idItem == null || quantidade == null || idUnidade == null) {
            logger.error("Valores de conversão inválidos");
            throw new FichaTecnicaException("Valores de conversão inválidos");
        }
        if (quantidade <= 0) {
            logger.error("Quantidade deve ser maior que zero");
            throw new FichaTecnicaException("Quantidade deve ser maior que zero");
        }
        return Boolean.TRUE;
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
