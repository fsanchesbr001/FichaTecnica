package com.fabriciosanches.fichatecnica.core.ports.out;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;

import java.util.List;
import java.util.Optional;

public interface ConversaoRepositoryPort {
    Conversao salvar(Conversao conversao);

    List<Conversao> buscarTodos();

    Optional<Conversao> buscarPorId(Long id);

    Optional<Conversao> buscarPorUnidadeDeEUnidadePara(Long unidadeDe, Long unidadePara);

    long contarPorUnidadeDeEUnidadePara(Long unidadeDe, Long unidadePara);

    List<ConversaoRelatorioDTO> buscarTodosComNomes();

    Optional<ConversaoRelatorioDTO> buscarPorIdComNomes(Long id);

    void deletar(Long id);
}
