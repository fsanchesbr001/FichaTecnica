package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.domains.UnidadeMedida;
import com.fabriciosanches.fichatecnica.domains.ItensProduto;

import java.math.BigDecimal;
import java.util.List;

public record ItensProdutoDTO(Long codigo, Long codProduto, Long codigoItem , Integer quantidade, BigDecimal valor,
                              UnidadeMedida unidadeMedida) {
    public ItensProdutoDTO(ItensProduto itensProduto) {
        this(itensProduto.getCodigo(),
                itensProduto.getCdProduto(),
                itensProduto.getCdItem(),
                itensProduto.getQuantidade(),
                itensProduto.getValor(),
                itensProduto.getUnidadeMedida());
    }

    public static List<ItensProdutoDTO> from(List<ItensProduto> lista) {
        return lista.stream().map(ItensProdutoDTO::new).toList();
    }
}
