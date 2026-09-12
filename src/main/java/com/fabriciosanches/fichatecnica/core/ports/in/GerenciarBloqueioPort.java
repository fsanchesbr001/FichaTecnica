package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.BloqueiosRequestDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.BloqueiosResponseDTO;

public interface GerenciarBloqueioPort {
    BloqueiosResponseDTO bloquear(BloqueiosRequestDTO bloqueiosRequestDTO);

    BloqueiosResponseDTO desbloquear(BloqueiosRequestDTO bloqueiosRequestDTO);
}


