package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.AtualizarUsuarioRequestDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UsuarioListagemDTO;

public interface AtualizarUsuarioPort {
    UsuarioListagemDTO atualizarUsuario(String email, AtualizarUsuarioRequestDTO dados);
}


