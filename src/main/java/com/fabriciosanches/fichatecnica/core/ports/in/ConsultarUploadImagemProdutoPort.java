package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UploadJobDTO;

public interface ConsultarUploadImagemProdutoPort {
    UploadJobDTO consultar(String jobId);
}


