package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.HistoricoItem;
import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.dtos.GraficoPrecoItemDTO;
import com.fabriciosanches.fichatecnica.dtos.HistoricoItemDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.HistoricoItemRepository;
import com.fabriciosanches.fichatecnica.repository.ItemRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class HistoricoItemService {

    private final Logger logger = LogManager.getLogger(HistoricoItemService.class);
    private final HistoricoItemRepository repository;
    private final ItemRepository itemRepository;

    public HistoricoItemService(HistoricoItemRepository repository, ItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
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

    /**
     * Busca o histórico de preços de um item ordenado por data de início (ascendente).
     *
     * @param codigoItem código do Item (campo cdItem no histórico)
     * @return lista de {@link HistoricoItemDTO} ordenada por dataInicio
     */
    public List<HistoricoItemDTO> buscarPorItemOrdenado(Long codigoItem) {
        logger.info("Buscando histórico ordenado por data para o item codigo={}", codigoItem);
        List<HistoricoItem> registros = repository.findByCdItemOrderByDataInicioAsc(codigoItem);
        if (registros.isEmpty()) {
            throw new FichaTecnicaException("Nenhum histórico encontrado para o item codigo=" + codigoItem);
        }
        return HistoricoItemDTO.from(registros);
    }

    /**
     * Gera o DTO de gráfico de variação de preços para um item.
     * A lista de pontos é obtida de {@link #buscarPorItemOrdenado(Long)}, ordenada por data.
     * Cada ponto contém: label (data formatada), valor bruto, valor formatado em BRL e
     * variação percentual em relação ao ponto anterior (primeiro ponto retorna "—").
     *
     * @param codigoItem código do Item (campo cdItem no histórico)
     * @return {@link GraficoPrecoItemDTO} pronto para consumo pelo Chart.js / ng2-charts
     */
    public GraficoPrecoItemDTO gerarGraficoPreco(Long codigoItem) {
        logger.info("Gerando gráfico de variação de preços para o item codigo={}", codigoItem);

        List<HistoricoItemDTO> historico = buscarPorItemOrdenado(codigoItem);

        // Busca nome do Item; usa "Item {id}" como fallback se não encontrado
        String nomeItem = itemRepository.findById(codigoItem)
                .map(Item::getNome)
                .orElse("Item " + codigoItem);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        NumberFormat brl = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        List<String> labels              = new ArrayList<>();
        List<BigDecimal> valores         = new ArrayList<>();
        List<String> valoresFormatados   = new ArrayList<>();
        List<String> variacoes           = new ArrayList<>();
        List<String> variacoesMonetarias = new ArrayList<>();

        BigDecimal anterior = null;

        for (HistoricoItemDTO h : historico) {
            labels.add(h.dataInicio() != null ? h.dataInicio().format(fmt) : "");
            valores.add(h.valor());
            valoresFormatados.add(h.valor() != null ? brl.format(h.valor()) : "—");

            if (anterior == null || h.valor() == null) {
                variacoes.add("—");
                variacoesMonetarias.add("—");
            } else if (anterior.compareTo(BigDecimal.ZERO) == 0) {
                variacoes.add("—");
                variacoesMonetarias.add("—");
            } else {
                // Variação percentual
                BigDecimal diff = h.valor().subtract(anterior);
                BigDecimal pct  = diff.divide(anterior, 4, RoundingMode.HALF_UP)
                                      .multiply(BigDecimal.valueOf(100));
                String sinalPct = pct.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
                variacoes.add(String.format("%s%,.1f%%", sinalPct, pct).replace(".", ","));

                // Variação monetária
                String sinalMon = diff.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "-";
                variacoesMonetarias.add(sinalMon + brl.format(diff.abs()));
            }

            anterior = h.valor();
        }

        String titulo = "Variação de Preço – " + nomeItem;
        logger.info("Gráfico gerado com {} pontos para o item codigo={} ({})", labels.size(), codigoItem, nomeItem);

        return new GraficoPrecoItemDTO(titulo, nomeItem, labels, valores, valoresFormatados, variacoes, variacoesMonetarias);
    }
}
