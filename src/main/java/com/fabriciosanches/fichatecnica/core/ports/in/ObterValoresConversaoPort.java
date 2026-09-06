package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresDTO;

public interface ObterValoresConversaoPort {
    ConversaoValoresDTO obterValoresConversao(Item item, Double quantidade, Long idUnidade);
}

