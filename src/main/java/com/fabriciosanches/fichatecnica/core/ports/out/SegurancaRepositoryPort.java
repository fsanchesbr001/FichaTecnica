package com.fabriciosanches.fichatecnica.core.ports.out;

import com.fabriciosanches.fichatecnica.core.domain.Seguranca;

import java.util.List;
import java.util.Optional;

public interface SegurancaRepositoryPort {
    Optional<Seguranca> buscarPorEmail(String email);

    String buscarCpfPorEmail(String email);

    Seguranca salvar(Seguranca seguranca);

    void deletar(Seguranca seguranca);

    List<Seguranca> buscarTodos();
}

