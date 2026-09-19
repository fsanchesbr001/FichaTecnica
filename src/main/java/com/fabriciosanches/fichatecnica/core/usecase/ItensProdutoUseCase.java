package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.ItemProdutoId;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.AdicionarItemAoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarQuantidadeItemDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CalcularValoresItensProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPizzaProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarItensDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarProdutosPorItemPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterDescricoesUnidadePort;
import com.fabriciosanches.fichatecnica.core.ports.in.RemoverItemDoProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.UnidadeMedidaRepositoryPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.GraficoPizzaDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.GraficoPizzaFatiaDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutosPorItemDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.QuantidadeValorDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItensProdutoUseCase implements AdicionarItemAoProdutoPort, ListarItensDoProdutoPort,
        ListarProdutosPorItemPort, CalcularValoresItensProdutoPort,
        RemoverItemDoProdutoPort, AtualizarQuantidadeItemDoProdutoPort,
        ObterDescricoesUnidadePort, GerarGraficoPizzaProdutoPort {

    private static final List<String> PIZZA_COLORS = List.of(
            "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF",
            "#FF9F40", "#C9CBCF", "#E7E9ED", "#71B37C", "#F7464A",
            "#46BFBD", "#FDB45C", "#949FB1", "#4D5360", "#AC64AD"
    );

    private final ItemProdutoRepositoryPort itemProdutoRepositoryPort;
    private final ProdutoRepositoryPort produtoRepositoryPort;
    private final ItemRepositoryPort itemRepositoryPort;
    private final UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort;
    private final ObterValoresConversaoPort obterValoresConversaoPort;

    public ItensProdutoUseCase(
            ItemProdutoRepositoryPort itemProdutoRepositoryPort,
            ProdutoRepositoryPort produtoRepositoryPort,
            ItemRepositoryPort itemRepositoryPort,
            UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort,
            ObterValoresConversaoPort obterValoresConversaoPort) {
        this.itemProdutoRepositoryPort = Objects.requireNonNull(itemProdutoRepositoryPort, "ItemProdutoRepositoryPort não pode ser nulo");
        this.produtoRepositoryPort = Objects.requireNonNull(produtoRepositoryPort, "ProdutoRepositoryPort não pode ser nulo");
        this.itemRepositoryPort = Objects.requireNonNull(itemRepositoryPort, "ItemRepositoryPort não pode ser nulo");
        this.unidadeMedidaRepositoryPort = Objects.requireNonNull(unidadeMedidaRepositoryPort, "UnidadeMedidaRepositoryPort não pode ser nulo");
        this.obterValoresConversaoPort = Objects.requireNonNull(obterValoresConversaoPort, "ObterValoresConversaoPort não pode ser nulo");
    }

    @Override
    public List<ItemProduto> adicionar(Long idProduto, List<ItemProduto> itemProduto) {
        Produto produto = getProduto(idProduto);

        if (!isValidItens(itemProduto)) {
            throw new FichaTecnicaException("Item não encontrado");
        }

        var salvoList = new ArrayList<ItemProduto>();
        for (var item : itemProduto) {
            Long itemId = item.getItem().getCodigo();
            Long unidadeId = item.getUnidadePara().getCodigo();

            Item itemDomain = itemRepositoryPort.buscarPorId(itemId)
                    .orElseThrow(() -> new FichaTecnicaException("Item não encontrado"));
            UnidadeMedida unidadeMedida = unidadeMedidaRepositoryPort.buscarPorId(unidadeId)
                    .orElseThrow(() -> new FichaTecnicaException("Unidade de medida não encontrada"));

            ConversaoValoresDTO conversaoValoresDTO = obterValoresConversaoPort.obterValoresConversao(itemDomain, item.getQuantidade(), unidadeId);
            ItemProduto itemProdutoSalvo = salvarItem(item, produto, itemDomain, unidadeMedida, conversaoValoresDTO);
            salvoList.add(itemProdutoSalvo);
        }

        atualizarValorItensProduto(produto);
        return salvoList;
    }

    @Override
    public List<ItemProduto> listar(Long idProduto) {
        Produto produto = getProduto(idProduto);

        return itemProdutoRepositoryPort.buscarPorProdutoId(idProduto).stream().peek(ip -> ip.setProduto(produto)).toList();
    }

    @Override
    public Map<Long, String> obter(List<Long> codigosUnidade) {
        if (codigosUnidade == null || codigosUnidade.isEmpty()) {
            return Collections.emptyMap();
        }

        return unidadeMedidaRepositoryPort.buscarTodos().stream()
                .filter(unidade -> codigosUnidade.contains(unidade.getCodigo()))
                .collect(Collectors.toMap(
                        UnidadeMedida::getCodigo,
                        unidade -> {
                            if (unidade.getNome() != null && !unidade.getNome().isBlank() && unidade.getSigla() != null && !unidade.getSigla().isBlank()) {
                                return unidade.getNome() + " (" + unidade.getSigla() + ")";
                            }
                            if (unidade.getNome() != null && !unidade.getNome().isBlank()) {
                                return unidade.getNome();
                            }
                            if (unidade.getSigla() != null && !unidade.getSigla().isBlank()) {
                                return unidade.getSigla();
                            }
                            return "-";
                        },
                        (atual, ignorar) -> atual,
                        LinkedHashMap::new
                ));
    }

    @Override
    public QuantidadeValorDTO calcular(Long idProduto) {
        logger().info("Inicio do metodo calcularQuantidadeEValorTotal");
        List<ItemProduto> listItensProduto = itemProdutoRepositoryPort.buscarPorProdutoId(idProduto);
        int quantidadeTotal = listItensProduto.size();
        BigDecimal valorTotal = listItensProduto.stream()
                .map(ItemProduto::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new QuantidadeValorDTO(quantidadeTotal, valorTotal);
    }

    @Override
    public List<ProdutosPorItemDTO> listarPorItem(Long codigoItem) {
        itemRepositoryPort.buscarPorId(codigoItem)
                .orElseThrow(() -> new FichaTecnicaException("Item não encontrado"));

        return itemProdutoRepositoryPort.buscarPorItemId(codigoItem).stream()
                .map(itemProduto -> new ProdutosPorItemDTO(
                        itemProduto.getProduto().getCodigo(),
                        itemProduto.getProduto().getNome()))
                .toList();
    }

    @Override
    public void remover(Long idProduto, Long idItem) {
        ItemProduto itemProduto = itemProdutoRepositoryPort.buscarPorProdutoIdEItemId(idProduto, idItem)
                .orElseThrow(() -> new FichaTecnicaException("ItemProduto não encontrado para o produto e item especificados"));
        itemProdutoRepositoryPort.deletarPorProdutoIdEItemId(idProduto, idItem);
        atualizarValorItensProduto(getProduto(idProduto));
    }

    @Override
    public void atualizarQuantidade(Long idProduto, Long idItem, Double novaQuantidade) {
        ItemProduto itemProduto = itemProdutoRepositoryPort.buscarPorProdutoIdEItemId(idProduto, idItem)
                .orElseThrow(() -> new FichaTecnicaException("ItemProduto não encontrado para o produto e item especificados"));

        Item item = itemRepositoryPort.buscarPorId(idItem)
                .orElseThrow(() -> new FichaTecnicaException("Item não encontrado"));

        ConversaoValoresDTO conversaoValoresDTO = obterValoresConversaoPort.obterValoresConversao(
                item,
                novaQuantidade,
                itemProduto.getUnidadePara().getCodigo());

        itemProduto.setQuantidade(conversaoValoresDTO.quantidade());
        itemProduto.setValor(conversaoValoresDTO.valor());
        itemProdutoRepositoryPort.salvar(itemProduto);
        atualizarValorItensProduto(getProduto(idProduto));
    }

    @Override
    public GraficoPizzaDTO gerar(Long idProduto) {
        Produto produto = getProduto(idProduto);
        List<ItemProduto> itensProduto = itemProdutoRepositoryPort.buscarPorProdutoId(idProduto);

        if (itensProduto.isEmpty()) {
            throw new FichaTecnicaException("Nenhum item encontrado para o produto id=" + idProduto);
        }

        NumberFormat brl = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        BigDecimal total = itensProduto.stream()
                .map(ItemProduto::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String valorTotalFormatado = brl.format(total);

        List<GraficoPizzaFatiaDTO> fatias = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        List<String> cores = new ArrayList<>();

        int colorIndex = 0;
        for (ItemProduto ip : itensProduto) {
            BigDecimal valorItem = ip.getValor() != null ? ip.getValor() : BigDecimal.ZERO;
            String nomeItem = ip.getItem().getNome();
            Long idItem = ip.getItem().getCodigo();
            String cor = PIZZA_COLORS.get(colorIndex % PIZZA_COLORS.size());

            double pct = total.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : valorItem.divide(total, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            String pctFormatada = String.format("%,.1f%%", pct).replace(".", ",");

            fatias.add(new GraficoPizzaFatiaDTO(
                    nomeItem,
                    idItem,
                    Math.round(pct * 100.0) / 100.0,
                    pctFormatada,
                    brl.format(valorItem),
                    valorItem,
                    valorTotalFormatado,
                    cor
            ));

            labels.add(nomeItem);
            valores.add(Math.round(pct * 100.0) / 100.0);
            cores.add(cor);
            colorIndex++;
        }

        return new GraficoPizzaDTO(produto.getNome(), valorTotalFormatado, fatias, labels, valores, cores);
    }

    private boolean isValidItens(List<ItemProduto> itemProduto) {
        var listItem = itemRepositoryPort.buscarTodos();
        var itemIdsCompleta = listItem.stream().map(Item::getCodigo).collect(Collectors.toSet());
        return itemProduto.stream().allMatch(item -> item.getItem() != null && item.getItem().getCodigo() != null && itemIdsCompleta.contains(item.getItem().getCodigo()));
    }

    private ItemProduto salvarItem(ItemProduto itemProduto, Produto produto, Item item, UnidadeMedida unidadeMedida, ConversaoValoresDTO conversaoValoresDTO) {
        ItemProduto itemProdutoSalvo = new ItemProduto();
        itemProdutoSalvo.setId(new ItemProdutoId(produto.getCodigo(), item.getCodigo()));
        itemProdutoSalvo.setProduto(produto);
        itemProdutoSalvo.setItem(item);
        itemProdutoSalvo.setQuantidade(itemProduto.getQuantidade());
        itemProdutoSalvo.setUnidadePara(unidadeMedida);
        itemProdutoSalvo.setValor(conversaoValoresDTO.valor());
        return itemProdutoRepositoryPort.salvar(itemProdutoSalvo);
    }

    private void atualizarValorItensProduto(Produto produto) {
        QuantidadeValorDTO quantidadeValorDTO = calcular(produto.getCodigo());
        produto.setValorItens(quantidadeValorDTO.valorTotal());
        produtoRepositoryPort.salvar(produto);
    }

    private Produto getProduto(Long idProduto) {
        return produtoRepositoryPort.buscarPorId(idProduto)
                .orElseThrow(() -> new FichaTecnicaException("Produto não encontrado"));
    }

    private org.apache.logging.log4j.Logger logger() {
        return org.apache.logging.log4j.LogManager.getLogger(ItensProdutoUseCase.class);
    }
}


