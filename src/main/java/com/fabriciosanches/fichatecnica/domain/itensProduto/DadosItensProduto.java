package com.fabriciosanches.fichatecnica.domain.itensProduto;

import com.fabriciosanches.fichatecnica.domain.medidas.UnidadeMedida;
import com.fabriciosanches.fichatecnica.domain.produto.Produto;

import java.math.BigDecimal;
import java.util.List;

public record DadosItensProduto(Long codigo, Long codProduto, Long codigoItem ,Integer quantidade, BigDecimal valor, UnidadeMedida unidadeMedida, Produto produto) {
    public DadosItensProduto(ItensProduto itensProduto) {
        this(itensProduto.getCodigo(),
                itensProduto.getCodigoProduto(),
                itensProduto.getCodigoItem(),
                itensProduto.getQuantidade(),
                itensProduto.getValor(),
                itensProduto.getUnidadeMedida(),
                itensProduto.getProduto());
    }

    public static List<DadosItensProduto> from(List<ItensProduto> lista) {
        return lista.stream().map(DadosItensProduto::new).toList();
    }
}
