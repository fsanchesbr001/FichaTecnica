package com.fabriciosanches.fichatecnica.core.ports.in;

import java.util.List;
import java.util.Map;

public interface ObterDescricoesUnidadePort {
    Map<Long, String> obter(List<Long> codigosUnidade);
}

