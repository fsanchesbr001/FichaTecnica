package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ConversaoRelatorioDTO;

import java.util.List;

public interface GerarRelatorioConversaoPort {
    List<ConversaoRelatorioDTO> buscarTodosComNomes();

    ConversaoRelatorioDTO buscarPorIdComNomes(Long id);
}

