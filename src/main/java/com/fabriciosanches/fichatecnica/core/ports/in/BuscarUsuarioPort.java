package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.UsuarioListagemDTO;

import java.util.List;

public interface BuscarUsuarioPort {
    UsuarioListagemDTO buscarUsuarioPorEmail(String email);

    List<UsuarioListagemDTO> listarTodosUsuarios();
}

