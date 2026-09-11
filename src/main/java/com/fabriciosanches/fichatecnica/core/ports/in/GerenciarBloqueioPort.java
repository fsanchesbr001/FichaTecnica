package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.BloqueiosRequestDTO;
import com.fabriciosanches.fichatecnica.dtos.BloqueiosResponseDTO;

public interface GerenciarBloqueioPort {
    BloqueiosResponseDTO bloquear(BloqueiosRequestDTO bloqueiosRequestDTO);

    BloqueiosResponseDTO desbloquear(BloqueiosRequestDTO bloqueiosRequestDTO);
}

