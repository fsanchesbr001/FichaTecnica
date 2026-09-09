package com.fabriciosanches.fichatecnica.core.ports.in;

import com.fabriciosanches.fichatecnica.dtos.UploadJobDTO;

import java.util.List;

public interface ListarJobsUploadImagemProdutoPort {
    List<UploadJobDTO> listar();
}

