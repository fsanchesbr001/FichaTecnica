package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UploadJobDTO;

import java.util.List;

public interface ListarJobsUploadImagemProdutoPort {
    List<UploadJobDTO> listar();
}


