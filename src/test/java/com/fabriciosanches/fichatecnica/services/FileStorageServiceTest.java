package com.fabriciosanches.fichatecnica.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService service;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        service = new FileStorageService();
        tempDir = Files.createTempDirectory("file-storage-test");
        ReflectionTestUtils.setField(service, "baseStoragePath", tempDir.toString());
    }

    @Test
    void storeFile_DeveSalvarArquivoComNomeUnicoERetornarCaminhoRelativo() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.png",
                "image/png",
                "conteudo".getBytes(StandardCharsets.UTF_8)
        );

        String relativePath = service.storeFile(file, "produtos/10", "foto.png");

        assertTrue(relativePath.startsWith("produtos"));
        assertTrue(relativePath.endsWith(".png"));
        Path saved = tempDir.resolve(relativePath);
        assertTrue(Files.exists(saved));
        assertEquals("conteudo", Files.readString(saved));
    }

    @Test
    void storeFile_DeveNormalizarDestinationPath() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.jpg",
                "image/jpeg",
                "abc".getBytes(StandardCharsets.UTF_8)
        );

        String relativePath = service.storeFile(file, "produtos/../produtos/11", "foto.jpg");

        assertTrue(relativePath.contains("produtos"));
        assertFalse(relativePath.contains(".."));
        assertTrue(Files.exists(tempDir.resolve(relativePath)));
    }

    @Test
    void storeFile_DeveGerarArquivoSemExtensaoQuandoNomeNaoContiverPonto() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "arquivo",
                "text/plain",
                "xyz".getBytes(StandardCharsets.UTF_8)
        );

        String relativePath = service.storeFile(file, "outros", "arquivo");

        assertFalse(relativePath.substring(relativePath.lastIndexOf('/') + 1).contains("."));
        assertTrue(Files.exists(tempDir.resolve(relativePath)));
    }
}

