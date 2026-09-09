package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.UploadJobDTO;

public interface ConsultarUploadImagemProdutoPort {
    UploadJobDTO consultar(String jobId);
}

