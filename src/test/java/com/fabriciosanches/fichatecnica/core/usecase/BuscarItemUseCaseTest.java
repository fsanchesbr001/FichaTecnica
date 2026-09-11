package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarItemUseCaseTest {

    @Mock
    private ItemRepositoryPort repositoryPort;

    @InjectMocks
    private BuscarItemUseCase useCase;

    @Test
    void listar_DeveOrdenarPorNomeCaseInsensitive() {
        UnidadeMedida unidade = new UnidadeMedida(1L, "Quilo", "kg");
        when(repositoryPort.buscarTodos()).thenReturn(List.of(
                new Item(2L, "farinha", unidade, new BigDecimal("2.00")),
                new Item(1L, "Acucar", unidade, new BigDecimal("1.00"))
        ));

        List<Item> result = useCase.listar();

        assertEquals(List.of("Acucar", "farinha"), result.stream().map(Item::getNome).toList());
    }

    @Test
    void buscarPorId_DeveLancarQuandoNaoEncontrado() {
        when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        FichaTecnicaException ex = assertThrows(FichaTecnicaException.class, () -> useCase.buscarPorId(99L));

        assertEquals("Item com ID 99 não encontrado", ex.getMessage());
    }
}

