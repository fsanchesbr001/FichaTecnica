package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.Usuario;

public interface AutenticarUsuarioPort {
    Usuario buscarPorLogin(String username);
}


