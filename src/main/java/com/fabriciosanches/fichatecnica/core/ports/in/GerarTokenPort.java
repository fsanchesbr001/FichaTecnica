package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.infrastructure.config.security.DadosTokenJWT;

public interface GerarTokenPort {
    DadosTokenJWT gerarToken(Usuario usuario);
}


