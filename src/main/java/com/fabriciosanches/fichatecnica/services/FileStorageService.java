package com.fabriciosanches.fichatecnica.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${digitalocean.storage.base-path:/var/www/uploads}")
    private String baseStoragePath;

    public String storeFile(MultipartFile file, String destinationPath, String fileName) throws IOException {
        // Normaliza o caminho para evitar directory traversal
        String safeDestinationPath = Paths.get(destinationPath).normalize().toString();

        // Cria o diretório de destino se não existir
        Path targetLocation = Paths.get(baseStoragePath, safeDestinationPath).toAbsolutePath().normalize();
        Files.createDirectories(targetLocation);

        // Gera um nome único para o arquivo para evitar sobrescrita
        String fileExtension = "";
        if (fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID() + fileExtension;

            // Copia o arquivo para o destino
            Path filePath = targetLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Retorna o caminho relativo para acesso via web
            return Paths.get(safeDestinationPath, uniqueFileName).toString();
        }
    }
