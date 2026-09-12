package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.storage;

import com.fabriciosanches.fichatecnica.core.ports.out.ArmazenamentoArquivoPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements ArmazenamentoArquivoPort {

    @Value("${digitalocean.storage.base-path:/var/www/uploads}")
    private String baseStoragePath;

    @Override
    public String salvar(byte[] conteudo, String destinationPath, String fileName) throws IOException {
        String safeDestinationPath = Paths.get(destinationPath).normalize().toString();
        Path targetLocation = Paths.get(baseStoragePath, safeDestinationPath).toAbsolutePath().normalize();
        Files.createDirectories(targetLocation);

        String fileExtension = "";
        if (fileName.contains(".")) {
            fileExtension = fileName.substring(fileName.lastIndexOf('.'));
        }

        String uniqueFileName = UUID.randomUUID() + fileExtension;
        Path filePath = targetLocation.resolve(uniqueFileName);
        Files.copy(new ByteArrayInputStream(conteudo), filePath, StandardCopyOption.REPLACE_EXISTING);

        return Paths.get(safeDestinationPath, uniqueFileName).toString();
    }

    @Override
    public byte[] carregar(String relativePath) throws IOException {
        Path filePath = Paths.get(baseStoragePath, relativePath).toAbsolutePath().normalize();
        return Files.readAllBytes(filePath);
    }

    @Override
    public void deletar(String relativePath) throws IOException {
        Path filePath = Paths.get(baseStoragePath, relativePath).toAbsolutePath().normalize();
        Files.deleteIfExists(filePath);
    }
}

