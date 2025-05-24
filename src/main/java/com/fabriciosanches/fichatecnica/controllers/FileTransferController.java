package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.FileTransferRequestDTO;
import com.fabriciosanches.fichatecnica.services.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileTransferController {
    private final FileStorageService fileStorageService;

    public FileTransferController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transferFile(@Valid @RequestPart("metadata") FileTransferRequestDTO request,
                @RequestPart("file") MultipartFile file) throws IOException {
        // Armazena o arquivo e obtém o caminho relativo
        String fileAccessPath = fileStorageService.storeFile(file,request.getDestinationPath(),
                    request.getFileName());

        // Cria a resposta
        Map<String, String> response = new HashMap<>();
        response.put("filePath", fileAccessPath);
        response.put("message", "Arquivo transferido com sucesso");

        return ResponseEntity.ok(response);
    }
}
