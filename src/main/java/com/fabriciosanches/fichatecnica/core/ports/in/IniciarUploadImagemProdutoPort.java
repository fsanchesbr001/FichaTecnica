package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.core.domain.ArquivoUpload;
import com.fabriciosanches.fichatecnica.dtos.UploadJobDTO;

public interface IniciarUploadImagemProdutoPort {
    UploadJobDTO iniciar(Long produtoId, ArquivoUpload arquivo);
}

