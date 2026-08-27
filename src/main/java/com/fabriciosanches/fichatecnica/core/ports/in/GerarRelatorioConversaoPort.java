package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;

import java.util.List;

public interface GerarRelatorioConversaoPort {
    List<ConversaoRelatorioDTO> buscarTodosComNomes();

    ConversaoRelatorioDTO buscarPorIdComNomes(Long id);
}
