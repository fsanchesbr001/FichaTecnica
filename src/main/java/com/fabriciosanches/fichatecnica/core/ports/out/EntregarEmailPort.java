package com.fabriciosanches.fichatecnica.core.ports.out;

import com.fabriciosanches.fichatecnica.core.domain.MensagemEmail;

public interface EntregarEmailPort {
    void entregar(MensagemEmail mensagem);
}
