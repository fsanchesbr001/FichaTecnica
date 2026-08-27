package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;

public interface AtualizarConversaoPort {
    Conversao atualizar(Long id, Conversao conversao);
}
