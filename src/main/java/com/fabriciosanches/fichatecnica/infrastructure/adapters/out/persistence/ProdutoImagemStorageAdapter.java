package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence;

import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoImagemStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class ProdutoImagemStorageAdapter implements ProdutoImagemStoragePort {
    @Value("${digitalocean.storage.base-path:/olivander/ficha_tecnica/imagens}")
    private String storagePath;

    @Value("${digitalocean.storage.public-url:http://localhost:8080/uploads}")
    private String publicUrl;

    @Override
    public String salvar(Long produtoId, String originalFilename, String contentType, byte[] content) {
        try {
            Path dir = Paths.get(storagePath, "produtos", String.valueOf(produtoId)).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String ext = obterExtensao(originalFilename);
            String nomeArquivo = UUID.randomUUID() + "." + ext;
            Path destino = dir.resolve(nomeArquivo);
            Files.copy(new ByteArrayInputStream(content), destino, StandardCopyOption.REPLACE_EXISTING);
            return publicUrl + "/produtos/" + produtoId + "/" + nomeArquivo;
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao salvar imagem do produto", e);
        }
    }

    @Override
    public void remover(String imagemUrl, Long produtoId) {
        try {
            String nomeArquivo = imagemUrl.substring(imagemUrl.lastIndexOf('/') + 1);
            Path arquivo = Paths.get(storagePath, "produtos", String.valueOf(produtoId), nomeArquivo).normalize();
            Files.deleteIfExists(arquivo);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao remover imagem do produto", e);
        }
    }

    private String obterExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return "jpg";
        }
        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1);
    }
}

