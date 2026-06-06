package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.*;
import com.fabriciosanches.fichatecnica.dtos.*;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ItemProdutoRepository;
import com.fabriciosanches.fichatecnica.repository.ItemRepository;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import com.fabriciosanches.fichatecnica.repository.UnidadeMedidaRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItensProdutoService {

    private final Logger logger = LogManager.getLogger(ItensProdutoService.class);

    private final ItemProdutoRepository itemProdutoRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemRepository itemRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;
    private final ConversaoService conversaoService;


    public ItensProdutoService(ItemProdutoRepository itemProdutoRepository,
                               ProdutoRepository produtoRepository,
                               ItemRepository itemRepository,
                               UnidadeMedidaRepository unidadeMedidaRepository,
                               ConversaoService conversaoService) {
        this.itemProdutoRepository = itemProdutoRepository;
        this.produtoRepository = produtoRepository;
        this.itemRepository = itemRepository;
        this.unidadeMedidaRepository = unidadeMedidaRepository;
        this.conversaoService = conversaoService;
    }

    public ItemProduto buscarPorId(Long id) {
        return itemProdutoRepository.findById(id).orElse(null);
    }

    public List<ProdutoCompletoDTO> salvar(Long idProduto, List<ItemProdutoDTO> itemProduto) {
        logger.info("Inicio do método salvar com Lista");
        Produto produto = getProduto(idProduto);

        if(!isValidItens(itemProduto)){
            logger.error("Item não encontrado");
            throw new FichaTecnicaException("Item não encontrado");
        }

        var produtoCompletoList = new ArrayList<ProdutoCompletoDTO>();
        for (var item: itemProduto) {
            var itemEntity = getItem(item.cdItem());
            var unidadeMedida = getUnidadeMedidaDTO(item);
            ConversaoValoresDTO conversaoValoresDTO = conversaoService.obterValoresConversao(itemEntity,
                    item.qtdItem(),item.cdUnidadeMedida());
            saveItem(item, produto, itemEntity, unidadeMedida, conversaoValoresDTO);



            produtoCompletoList.add(new ProdutoCompletoDTO(produto.getNome(), itemEntity.getNome(),
                    itemEntity.getCodigo(), item.qtdItem(), item.cdUnidadeMedida(),
                    conversaoValoresDTO.valor()));
        }

        QuantidadeValorDTO quantidadeValorDTO = calcularQuantidadeEValorTotal(produto.getCodigo());
        produto.setValorItens(quantidadeValorDTO.valorTotal());
        produtoRepository.save(produto);

        return produtoCompletoList;
    }

    public List<ProdutoCompletoDTO> listarItensProduto(Long idProduto) {
        logger.info("Inicio do método listarItensProduto");
        Produto produto = getProduto(idProduto);

        return produto.getProdutosList().stream().map(ip -> new ProdutoCompletoDTO(
                ip.getProduto().getNome(),
                ip.getItem().getNome(), ip.getItem().getCodigo(), ip.getQuantidade(),
                ip.getUnidadePara().getCodigo(), ip.getValor())
        ).collect(Collectors.toList());
    }

    /**
     * Resolve as descrições de unidade para exibição no relatório.
     * Formato preferencial: "Nome (sigla)".
     */
    public Map<Long, String> obterDescricoesUnidade(List<Long> codigosUnidade) {
        if (codigosUnidade == null || codigosUnidade.isEmpty()) {
            return Collections.emptyMap();
        }

        return unidadeMedidaRepository.findAllById(codigosUnidade)
                .stream()
                .collect(Collectors.toMap(
                        UnidadeMedida::getCodigo,
                        unidade -> {
                            String nome = unidade.getNome();
                            String sigla = unidade.getSigla();
                            if (nome != null && !nome.isBlank() && sigla != null && !sigla.isBlank()) {
                                return nome + " (" + sigla + ")";
                            }
                            if (nome != null && !nome.isBlank()) {
                                return nome;
                            }
                            if (sigla != null && !sigla.isBlank()) {
                                return sigla;
                            }
                            return "-";
                        },
                        (atual, ignorar) -> atual
                ));
    }

    public QuantidadeValorDTO calcularQuantidadeEValorTotal(Long idProduto) {
        logger.info("Inicio do método calcularQuantidadeEValorTotal");
        int quantidadeTotal = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;

        List<ItemProduto> listItensProduto = itemProdutoRepository.findByProdutoCodigo(idProduto);

        for (ItemProduto itemProduto : listItensProduto) {
            quantidadeTotal += 1;
            valorTotal = valorTotal.add(itemProduto.getValor());
        }
        return new QuantidadeValorDTO(quantidadeTotal, valorTotal);
    }

    public List<ProdutosPorItemDTO> listarProdutosPorItem(Long codigoItem) {
        logger.info("Inicio do método listarProdutosPorItem");
        Item item = itemRepository.findById(codigoItem)
                .orElseThrow(() -> {
                    logger.error("Item não encontrado");
                    return new FichaTecnicaException("Item não encontrado");
                });

        return itemProdutoRepository.findByItem(item).stream()
                .map(itemProduto -> new ProdutosPorItemDTO(
                        itemProduto.getProduto().getCodigo(),
                        itemProduto.getProduto().getNome()
                ))
                .collect(Collectors.toList());
    }

    public void deletarItemProduto(Long idProduto, Long idItem) {
        logger.info("Inicio do método deletarItemProduto");

        ItemProduto itemProduto = itemProdutoRepository.findByProdutoCodigoAndItemCodigo(idProduto, idItem);

        itemProdutoRepository.delete(itemProduto);

        QuantidadeValorDTO quantidadeValorDTO = calcularQuantidadeEValorTotal(idProduto);

        var produto = getProduto(idProduto);
        produto.setValorItens(quantidadeValorDTO.valorTotal());
        produtoRepository.save(produto);
    }

    // ── Paleta de cores para o gráfico de pizza (Chart.js) ───────────────────
    private static final List<String> PIZZA_COLORS = List.of(
            "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF",
            "#FF9F40", "#C9CBCF", "#E7E9ED", "#71B37C", "#F7464A",
            "#46BFBD", "#FDB45C", "#949FB1", "#4D5360", "#AC64AD"
    );

    /**
     * Gera o DTO de gráfico de pizza com a composição percentual de custo de cada
     * item de um produto. Cada fatia contém porcentagem, valor do item, valor total
     * e cor para exibição no tooltip do Chart.js / ng2-charts.
     *
     * @param idProduto código do Produto
     * @return {@link GraficoPizzaDTO} pronto para consumo pelo frontend Angular
     */
    public GraficoPizzaDTO gerarGraficoPizza(Long idProduto) {
        logger.info("Gerando gráfico de pizza para o produto id={}", idProduto);

        Produto produto = getProduto(idProduto);
        List<ItemProduto> itensProduto = itemProdutoRepository.findByProdutoCodigo(idProduto);

        if (itensProduto.isEmpty()) {
            throw new FichaTecnicaException("Nenhum item encontrado para o produto id=" + idProduto);
        }

        NumberFormat brl = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Calcula o valor total somando todos os itens
        BigDecimal total = itensProduto.stream()
                .map(ItemProduto::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String valorTotalFormatado = brl.format(total);

        List<GraficoPizzaFatiaDTO> fatias   = new ArrayList<>();
        List<String>               labels   = new ArrayList<>();
        List<Double>               valores  = new ArrayList<>();
        List<String>               cores    = new ArrayList<>();

        int colorIndex = 0;
        for (ItemProduto ip : itensProduto) {
            BigDecimal valorItem = ip.getValor() != null ? ip.getValor() : BigDecimal.ZERO;
            String nomeItem      = ip.getItem().getNome();
            Long   idItem        = ip.getItem().getCodigo();
            String cor           = PIZZA_COLORS.get(colorIndex % PIZZA_COLORS.size());

            // Percentual desta fatia = (valorItem / total) * 100
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

        logger.info("Gráfico de pizza gerado com {} fatias para o produto id={}", fatias.size(), idProduto);

        return new GraficoPizzaDTO(produto.getNome(), valorTotalFormatado, fatias, labels, valores, cores);
    }

    public void atualizarQuantidadeItemProduto(Long idProduto, Long idItem, Double novaQuantidade) {
        logger.info("Inicio do método atualizarQuantidadeItemProduto");

        ItemProduto itemProduto = itemProdutoRepository.findByProdutoCodigoAndItemCodigo(idProduto, idItem);
        if (itemProduto == null) {
            throw new FichaTecnicaException("ItemProduto não encontrado para o produto e item especificados");
        }

        ItemProdutoDTO itemProdutoDTO = new ItemProdutoDTO(
                itemProduto.getProduto().getCodigo(),
                itemProduto.getItem().getCodigo(),
                novaQuantidade,
                itemProduto.getUnidadePara().getCodigo(),
                itemProduto.getValor());

        ConversaoValoresDTO conversaoValoresDTO = conversaoService.obterValoresConversao(
                itemProduto.getItem(),
                itemProdutoDTO.qtdItem(),
                itemProdutoDTO.cdUnidadeMedida());

        itemProduto.setQuantidade(conversaoValoresDTO.quantidade());
        itemProduto.setValor(conversaoValoresDTO.valor());
        itemProdutoRepository.save(itemProduto);

        var produto = getProduto(idProduto);
        QuantidadeValorDTO quantidadeValorDTO = calcularQuantidadeEValorTotal(produto.getCodigo());
        produto.setValorItens(quantidadeValorDTO.valorTotal());
        produtoRepository.save(produto);

        logger.info("Quantidade do ItemProduto atualizada com sucesso");
    }

    private Boolean isValidItens(List<ItemProdutoDTO> itemProduto) {
        var listItem = itemRepository.findAll();
        var itemIdsCompleta = listItem.stream().map(Item::getCodigo)
                .collect(Collectors.toSet());

        return  itemProduto.stream().allMatch(item -> itemIdsCompleta.contains(item.cdItem()));
    }

    private void saveItem(ItemProdutoDTO itemProduto, Produto produto, Item item,
                          UnidadeMedidaDTO unidadeMedida, ConversaoValoresDTO conversaoValoresDTO) {
        var itemProdutoSalvo = new ItemProduto();
        itemProdutoSalvo.setId(new ItemProdutoId(produto.getCodigo(), item.getCodigo()));
        itemProdutoSalvo.setProduto(produto);
        itemProdutoSalvo.setItem(item);
        itemProdutoSalvo.setQuantidade(itemProduto.qtdItem());
        itemProdutoSalvo.setUnidadePara(UnidadeMedidaDTO.toEntity(unidadeMedida));
        itemProdutoSalvo.setValor(conversaoValoresDTO.valor());
        itemProdutoRepository.save(itemProdutoSalvo);
    }

    private Produto getProduto(Long idProduto) {
        return produtoRepository.findById(idProduto)
                .orElseThrow( () -> {
                    logger.error("Produto não encontrado");
                    return new FichaTecnicaException("Produto não encontrado");
                });
    }

    private UnidadeMedidaDTO getUnidadeMedidaDTO(ItemProdutoDTO itemProduto) {
        UnidadeMedida unidadeMedida = unidadeMedidaRepository.findById(itemProduto.cdUnidadeMedida())
                .orElseThrow(() -> {
                    logger.error("Unidade de medida não encontrada");
                    return new FichaTecnicaException("Unidade de medida não encontrada");
                });
        return new UnidadeMedidaDTO(unidadeMedida);
    }

    private Item getItem(Long  idItem) {
        return itemRepository.findById(idItem)
                .orElseThrow();
    }
}
