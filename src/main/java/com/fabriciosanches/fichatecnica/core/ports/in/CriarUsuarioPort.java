package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RegisterDTO;
import jakarta.mail.MessagingException;

public interface CriarUsuarioPort {
    void registrarUsuario(RegisterDTO registerDTO) throws MessagingException;
}


