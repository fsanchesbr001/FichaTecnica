package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;
import com.fabriciosanches.fichatecnica.security.DadosTokenJWT;

public interface GerarTokenPort {
    DadosTokenJWT gerarToken(Usuario usuario);
}

