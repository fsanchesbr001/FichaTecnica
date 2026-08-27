package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnidadeMedidaPersistenceAdapterTest {

    @Mock
    private SpringDataUnidadeMedidaRepository repository;

    @InjectMocks
    private UnidadeMedidaPersistenceAdapter adapter;

    @Test
    void salvar_DeveMapearDominioParaEntidadeERetornarDominio() {
        UnidadeMedida domain = new UnidadeMedida(1L, "Quilograma", "KG");
        when(repository.save(org.mockito.ArgumentMatchers.any(UnidadeMedidaEntity.class)))
                .thenReturn(new UnidadeMedidaEntity(1L, "Quilograma", "KG"));

        UnidadeMedida salva = adapter.salvar(domain);

        ArgumentCaptor<UnidadeMedidaEntity> captor = ArgumentCaptor.forClass(UnidadeMedidaEntity.class);
        verify(repository).save(captor.capture());
        assertEquals("KG", captor.getValue().getSigla());
        assertEquals("Quilograma", salva.getNome());
    }

    @Test
    void buscarTodos_DeveMapearEntidadesParaDominio() {
        when(repository.findAll()).thenReturn(List.of(
                new UnidadeMedidaEntity(1L, "Quilograma", "KG"),
                new UnidadeMedidaEntity(2L, "Grama", "G")
        ));

        List<UnidadeMedida> resultado = adapter.buscarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Grama", resultado.get(1).getNome());
    }

    @Test
    void buscarPorSigla_DeveMapearQuandoEncontrar() {
        when(repository.findBySigla("KG")).thenReturn(Optional.of(new UnidadeMedidaEntity(1L, "Quilograma", "KG")));

        Optional<UnidadeMedida> resultado = adapter.buscarPorSigla("KG");

        assertEquals(true, resultado.isPresent());
        assertEquals(1L, resultado.orElseThrow().getCodigo());
    }

    @Test
    void deletar_DeveLancarIllegalStateQuandoViolacaoDeIntegridade() {
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("fk")).when(repository).flush();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> adapter.deletar(1L));

        assertEquals("FKC-Registro não pode ser deletado. Existem Conversões vinculadas.", exception.getMessage());
    }
}
