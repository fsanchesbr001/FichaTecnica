package com.fabriciosanches.fichatecnica.dtos;

import java.math.BigDecimal;

public record ConversaoValoresDTO(Integer quantidade, Long unidadeMedidaPara,
                                  BigDecimal valor) {
}
