package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.HistoricoItem;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RegistrarHistoricoItemPort {
    HistoricoItem registrar(Long codigoItem, BigDecimal valor, LocalDate dataInicio);
}


