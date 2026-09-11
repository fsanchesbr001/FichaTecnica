package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.AtualizarUsuarioRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.UsuarioListagemDTO;

public interface AtualizarUsuarioPort {
    UsuarioListagemDTO atualizarUsuario(String email, AtualizarUsuarioRequestDTO dados);
}

