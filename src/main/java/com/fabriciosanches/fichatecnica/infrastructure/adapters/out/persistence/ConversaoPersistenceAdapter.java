package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.core.ports.out.ConversaoRepositoryPort;
import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ConversaoPersistenceAdapter implements ConversaoRepositoryPort {
    private final SpringDataConversaoRepository repository;

    public ConversaoPersistenceAdapter(SpringDataConversaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Conversao salvar(Conversao conversao) {
        ConversaoEntity entidade = toEntity(conversao);
        ConversaoEntity salva = repository.save(entidade);
        return toDomain(salva);
    }

    @Override
    public List<Conversao> buscarTodos() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Conversao> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Conversao> buscarPorUnidadeDeEUnidadePara(Long unidadeDe, Long unidadePara) {
        return repository.findByUnidadeDeAndUnidadePara(unidadeDe, unidadePara).map(this::toDomain);
    }

    @Override
    public long contarPorUnidadeDeEUnidadePara(Long unidadeDe, Long unidadePara) {
        return repository.countByUnidadeDeAndUnidadePara(unidadeDe, unidadePara);
    }

    @Override
    public List<ConversaoRelatorioDTO> buscarTodosComNomes() {
        return repository.findAllComNomes();
    }

    @Override
    public Optional<ConversaoRelatorioDTO> buscarPorIdComNomes(Long id) {
        return repository.findByIdComNomes(id);
    }

    @Override
    public void deletar(Long id) {
        repository.deleteById(id);
    }

    private Conversao toDomain(ConversaoEntity entidade) {
        return new Conversao(
                entidade.getCodigo(),
                entidade.getUnidadeDe(),
                entidade.getUnidadePara(),
                entidade.getOperacao(),
                entidade.getValor()
        );
    }

    private ConversaoEntity toEntity(Conversao domain) {
        return new ConversaoEntity(
                domain.getCodigo(),
                domain.getUnidadeDe(),
                domain.getUnidadePara(),
                domain.getOperacao(),
                domain.getValor()
        );
    }
}
