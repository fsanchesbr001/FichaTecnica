package com.fabriciosanches.fichatecnica.dtos;

import java.math.BigDecimal;

public record ConversaoValoresDTO(Double quantidade, Long unidadeMedidaPara,
                                  BigDecimal valor) {
}
