package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.HistoricoItem;
import com.fabriciosanches.fichatecnica.core.ports.out.HistoricoItemRepositoryPort;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarHistoricoItemUseCaseTest {

    @Mock
    private HistoricoItemRepositoryPort historicoItemRepositoryPort;

    @InjectMocks
    private RegistrarHistoricoItemUseCase useCase;

    @Test
    void registrar_DevePersistirHistoricoComDataInformada() {
        when(historicoItemRepositoryPort.salvar(any(HistoricoItem.class)))
                .thenReturn(new HistoricoItem(1L, 10L, new BigDecimal("100.00"), LocalDate.of(2026, 9, 5)));

        LocalDate data = LocalDate.of(2026, 9, 5);
        HistoricoItem result = useCase.registrar(10L, new BigDecimal("100.00"), data);

        assertEquals(10L, result.getCdItem());
        assertEquals(data, result.getDataInicio());
    }

    @Test
    void registrar_DeveLancarExcecaoQuandoCodigoNulo() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.registrar(null, new BigDecimal("10.00"), LocalDate.now())
        );

        assertEquals("CÃ³digo do item nÃ£o pode ser nulo", ex.getMessage());
    }
}


