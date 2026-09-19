package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.RegistrarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarItemUseCaseTest {

    @Mock
    private ItemRepositoryPort itemRepositoryPort;

    @Mock
    private RegistrarHistoricoItemPort registrarHistoricoItemPort;

    @InjectMocks
    private CriarItemUseCase useCase;

    @Test
    void criar_DeveSalvarERegistrarHistorico() {
        UnidadeMedida unidade = new UnidadeMedida(1L, "Quilo", "kg");
        BigDecimal valor = new BigDecimal("90.00");

        when(itemRepositoryPort.contarPorNome("Farinha")).thenReturn(0L);
        when(itemRepositoryPort.salvar(any(Item.class)))
                .thenReturn(new Item(10L, "Farinha", unidade, valor));

        Item result = useCase.criar("Farinha", unidade, valor);

        assertEquals(10L, result.getCodigo());
        verify(registrarHistoricoItemPort).registrar(eq(10L), eq(valor), any(LocalDate.class));
    }

    @Test
    void criar_DeveLancarExcecaoQuandoNomeDuplicado() {
        when(itemRepositoryPort.contarPorNome("Farinha")).thenReturn(1L);

        FichaTecnicaException ex = assertThrows(
                FichaTecnicaException.class,
                () -> useCase.criar("Farinha", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("10.00"))
        );

        assertEquals("Item já cadastrado", ex.getMessage());
    }
}


