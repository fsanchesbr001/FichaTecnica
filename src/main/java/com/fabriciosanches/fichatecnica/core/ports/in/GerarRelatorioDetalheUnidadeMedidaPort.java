package com.fabriciosanches.fichatecnica.core.ports.in;

public interface GerarRelatorioDetalheUnidadeMedidaPort {
    byte[] executar(String siglaOuCodigo);
}
