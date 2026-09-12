package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileTransferRequestDTO {
    @NotBlank(message = "O nome do arquivo Ã© obrigatÃ³rio")
    private String fileName;

    @NotBlank(message = "O caminho de destino Ã© obrigatÃ³rio")
    private String destinationPath;

    private MultipartFile file;


}


