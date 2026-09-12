package com.fabriciosanches.fichatecnica.core.ports.out;

public interface ProdutoImagemStoragePort {
    String salvar(Long produtoId, String originalFilename, String contentType, byte[] content);

    void remover(String imagemUrl, Long produtoId);
}


