package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.UploadJobDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IniciarUploadImagemProdutoPort {
    UploadJobDTO iniciar(Long produtoId, MultipartFile file);
}

