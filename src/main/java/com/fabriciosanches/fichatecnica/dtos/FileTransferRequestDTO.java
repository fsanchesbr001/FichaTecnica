package com.fabriciosanches.fichatecnica.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileTransferRequestDTO {
    @NotBlank(message = "O nome do arquivo é obrigatório")
    private String fileName;

    @NotBlank(message = "O caminho de destino é obrigatório")
    private String destinationPath;

    private MultipartFile file;


}

