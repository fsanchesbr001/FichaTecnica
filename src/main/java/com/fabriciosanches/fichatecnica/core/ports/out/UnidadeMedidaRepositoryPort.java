package com.fabriciosanches.fichatecnica.core.ports.out;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;

import java.util.List;
import java.util.Optional;

public interface UnidadeMedidaRepositoryPort {
    UnidadeMedida salvar(UnidadeMedida unidadeMedida);

    List<UnidadeMedida> buscarTodos();

    Optional<UnidadeMedida> buscarPorId(Long id);

    Optional<UnidadeMedida> buscarPorSigla(String sigla);

    void deletar(Long id);
}
