package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.HistoricoItem;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.HistoricoItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.GraficoPrecoItemDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ListarHistoricoItemUseCase implements ListarHistoricoItemPort {
    private final HistoricoItemRepositoryPort historicoItemRepositoryPort;
    private final ItemRepositoryPort itemRepositoryPort;

    public ListarHistoricoItemUseCase(
            HistoricoItemRepositoryPort historicoItemRepositoryPort,
            ItemRepositoryPort itemRepositoryPort) {
        this.historicoItemRepositoryPort = Objects.requireNonNull(historicoItemRepositoryPort, "HistoricoItem repository port nÃ£o pode ser nulo");
        this.itemRepositoryPort = Objects.requireNonNull(itemRepositoryPort, "Item repository port nÃ£o pode ser nulo");
    }

    @Override
    public List<HistoricoItem> listar() {
        return historicoItemRepositoryPort.buscarTodos();
    }

    @Override
    public HistoricoItem buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id nÃ£o pode ser nulo");
        }

        return historicoItemRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new FichaTecnicaException("Historico de item nÃ£o encontrado"));
    }

    @Override
    public List<HistoricoItem> listarPorCodigoItem(Long codigoItem) {
        return historicoItemRepositoryPort.buscarPorCodigoItem(codigoItem);
    }

    @Override
    public List<HistoricoItem> listarPorCodigoItemOrdenadoPorDataInicio(Long codigoItem) {
        List<HistoricoItem> registros = historicoItemRepositoryPort.buscarPorCodigoItemOrdenadoPorDataInicio(codigoItem);
        if (registros.isEmpty()) {
            throw new FichaTecnicaException("Nenhum histÃ³rico encontrado para o item codigo=" + codigoItem);
        }
        return registros;
    }

    @Override
    public GraficoPrecoItemDTO gerarGraficoPreco(Long codigoItem) {
        List<HistoricoItem> historico = listarPorCodigoItemOrdenadoPorDataInicio(codigoItem);

        String nomeItem = itemRepositoryPort.buscarPorId(codigoItem)
                .map(item -> item.getNome())
                .orElse("Item " + codigoItem);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        NumberFormat brl = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> valores = new ArrayList<>();
        List<String> valoresFormatados = new ArrayList<>();
        List<String> variacoes = new ArrayList<>();
        List<String> variacoesMonetarias = new ArrayList<>();

        BigDecimal anterior = null;

        for (HistoricoItem h : historico) {
            String dataLabel = h.getDataInicio() != null ? h.getDataInicio().format(fmt) : "";
            String labelEvento = h.getCodigo() != null ? dataLabel + " [#" + h.getCodigo() + "]" : dataLabel;
            labels.add(labelEvento);
            valores.add(h.getValor());
            valoresFormatados.add(h.getValor() != null ? brl.format(h.getValor()) : "â€”");

            if (anterior == null || h.getValor() == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
                variacoes.add("â€”");
                variacoesMonetarias.add("â€”");
            } else {
                BigDecimal diff = h.getValor().subtract(anterior);
                BigDecimal pct = diff.divide(anterior, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                String sinalPct = pct.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
                variacoes.add(String.format("%s%,.1f%%", sinalPct, pct).replace(".", ","));

                String sinalMon = diff.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "-";
                variacoesMonetarias.add(sinalMon + brl.format(diff.abs()));
            }

            anterior = h.getValor();
        }

        String titulo = "Variacao de Preco - " + nomeItem;
        return new GraficoPrecoItemDTO(titulo, nomeItem, labels, valores, valoresFormatados, variacoes, variacoesMonetarias);
    }
}


