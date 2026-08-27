package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;

import java.util.List;

public interface BuscarConversaoPort {
    List<Conversao> buscarTodos();

    Conversao buscarPorId(Long id);
}
