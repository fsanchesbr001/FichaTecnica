package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.Conversao;
import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversaoPersistenceAdapterTest {

    @Mock
    private SpringDataConversaoRepository repository;

    @InjectMocks
    private ConversaoPersistenceAdapter adapter;

    @Test
    void salvar_DeveMapearDominioParaEntidadeERetornarDominio() {
        Conversao domain = new Conversao(1L, 2L, 3L, "MULTIPLICA", new BigDecimal("2.00"));
        when(repository.save(org.mockito.ArgumentMatchers.any(ConversaoEntity.class)))
                .thenReturn(new ConversaoEntity(1L, 2L, 3L, "MULTIPLICA", new BigDecimal("2.00")));

        Conversao salvo = adapter.salvar(domain);

        ArgumentCaptor<ConversaoEntity> captor = ArgumentCaptor.forClass(ConversaoEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(2L, captor.getValue().getUnidadeDe());
        assertEquals(1L, salvo.getCodigo());
    }

    @Test
    void buscarTodos_DeveMapearEntidadesParaDominio() {
        when(repository.findAll()).thenReturn(List.of(
                new ConversaoEntity(1L, 2L, 3L, "MULTIPLICA", BigDecimal.ONE),
                new ConversaoEntity(2L, 3L, 4L, "DIVIDE", BigDecimal.TEN)
        ));

        List<Conversao> resultado = adapter.buscarTodos();

        assertEquals(2, resultado.size());
        assertEquals("DIVIDE", resultado.get(1).getOperacao());
    }

    @Test
    void buscarPorUnidadeDeEUnidadePara_DeveRetornarDominioMapeado() {
        when(repository.findByUnidadeDeAndUnidadePara(2L, 3L))
                .thenReturn(Optional.of(new ConversaoEntity(1L, 2L, 3L, "MULTIPLICA", BigDecimal.ONE)));

        Optional<Conversao> resultado = adapter.buscarPorUnidadeDeEUnidadePara(2L, 3L);

        assertEquals(true, resultado.isPresent());
        assertEquals(1L, resultado.orElseThrow().getCodigo());
    }

    @Test
    void buscarTodosComNomes_DeveDelegarParaRepository() {
        when(repository.findAllComNomes()).thenReturn(List.of(
                new ConversaoRelatorioDTO(1L, "Quilo", "Grama", "MULTIPLICA", new BigDecimal("1000.00"))
        ));

        List<ConversaoRelatorioDTO> resultado = adapter.buscarTodosComNomes();

        assertEquals(1, resultado.size());
        assertEquals("Grama", resultado.get(0).unidadePara());
    }
}
