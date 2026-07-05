package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.report;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.out.GeradorRelatorioUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.enums.TipoRelatorio;
import com.fabriciosanches.fichatecnica.services.RelatorioService;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ITextUnidadeMedidaReportAdapter implements GeradorRelatorioUnidadeMedidaPort {

    private final RelatorioService relatorioService;
    private final Gson gson = new Gson();

    public ITextUnidadeMedidaReportAdapter(RelatorioService relatorioService) {
        this.relatorioService = Objects.requireNonNull(relatorioService, "RelatorioService não pode ser nulo");
    }

    @Override
    public byte[] gerarRelatorioLista(List<UnidadeMedida> unidades) {
        List<Map<String, Object>> dados = unidades.stream()
                .map(this::toMap)
                .toList();

        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("codigo", "Código");
        colunas.put("nome", "Nome");
        colunas.put("sigla", "Sigla");

        RelatorioRequestDTO request = new RelatorioRequestDTO(
                gson.toJson(dados),
                "",
                "Lista de Unidades de Medida",
                colunas,
                TipoRelatorio.LISTA,
                OrientacaoRelatorio.RETRATO,
                true
        );

        return gerarPdf(request, "lista");
    }

    @Override
    public byte[] gerarRelatorioDetalhe(UnidadeMedida unidade) {
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("codigo", "Código");
        colunas.put("nome", "Nome");
        colunas.put("sigla", "Sigla");

        RelatorioRequestDTO request = new RelatorioRequestDTO(
                gson.toJson(List.of(toMap(unidade))),
                "",
                "Detalhe de Unidade de Medida",
                colunas,
                TipoRelatorio.DETALHE,
                OrientacaoRelatorio.PAISAGEM,
                false
        );

        return gerarPdf(request, "detalhe");
    }

    private Map<String, Object> toMap(UnidadeMedida unidade) {
        Map<String, Object> linha = new LinkedHashMap<>();
        linha.put("codigo", unidade.getCodigo());
        linha.put("nome", unidade.getNome());
        linha.put("sigla", unidade.getSigla());
        return linha;
    }

    private byte[] gerarPdf(RelatorioRequestDTO request, String tipo) {
        try {
            return relatorioService.gerarRelatorioPDF(request);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao gerar relatório PDF de " + tipo + " de unidade de medida", e);
        }
    }
}
