package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.controllers.FileTransferController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileTransferControllerTest {

    private MockMvc mockMvc;
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = Mockito.mock(FileStorageService.class);
        FileTransferController controller = new FileTransferController(fileStorageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void transferFile_DeveRetornarOkQuandoUploadConcluir() throws Exception {
        when(fileStorageService.storeFile(any(), eq("/tmp"), eq("foto.png")))
                .thenReturn("/tmp/foto.png");

        MockMultipartFile metadata = new MockMultipartFile(
                "metadata",
                "metadata.json",
                "application/json",
                """
                        {
                          "fileName":"foto.png",
                          "destinationPath":"/tmp"
                        }
                        """.getBytes()
        );

        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", "abc".getBytes());

        mockMvc.perform(multipart("/api/files/transfer")
                        .file(metadata)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath").value("/tmp/foto.png"))
                .andExpect(jsonPath("$.message").value("Arquivo transferido com sucesso"));
    }

    @Test
    void transferFile_DevePropagarIOExceptionQuandoServicoFalhar() throws Exception {
        when(fileStorageService.storeFile(any(), any(), any())).thenThrow(new IOException("falha"));

        MockMultipartFile metadata = new MockMultipartFile(
                "metadata",
                "metadata.json",
                "application/json",
                """
                        {
                          "fileName":"foto.png",
                          "destinationPath":"/tmp"
                        }
                        """.getBytes()
        );

        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", "abc".getBytes());

        Exception ex = assertThrows(Exception.class, () ->
                mockMvc.perform(multipart("/api/files/transfer")
                        .file(metadata)
                        .file(file))
        );

        assertTrue(ex.getMessage().contains("falha"));
    }
}

