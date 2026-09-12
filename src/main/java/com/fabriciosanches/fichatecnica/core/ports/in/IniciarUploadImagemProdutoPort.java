package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.ArquivoUpload;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UploadJobDTO;

public interface IniciarUploadImagemProdutoPort {
    UploadJobDTO iniciar(Long produtoId, ArquivoUpload arquivo);
}


