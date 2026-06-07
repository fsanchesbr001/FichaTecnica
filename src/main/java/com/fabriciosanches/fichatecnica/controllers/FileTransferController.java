package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.FileTransferRequestDTO;
import com.fabriciosanches.fichatecnica.services.FileStorageService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Arquivos", description = "Transferência de arquivos e armazenamento no backend")
public class FileTransferController {
    private final FileStorageService fileStorageService;

    public FileTransferController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfere arquivo", description = "Recebe metadados e um arquivo multipart e o armazena no destino informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo transferido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para transferência"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao transferir o arquivo")
    })
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
