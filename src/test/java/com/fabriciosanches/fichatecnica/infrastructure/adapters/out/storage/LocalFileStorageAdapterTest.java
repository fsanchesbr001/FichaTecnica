package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageAdapterTest {

    private LocalFileStorageAdapter adapter;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        adapter = new LocalFileStorageAdapter();
        tempDir = Files.createTempDirectory("file-storage-test");
        ReflectionTestUtils.setField(adapter, "baseStoragePath", tempDir.toString());
    }

    @Test
    void salvar_DevePersistirArquivoERetornarCaminhoRelativo() throws IOException {
        String relativePath = adapter.salvar("conteudo".getBytes(StandardCharsets.UTF_8), "produtos/10", "foto.png");

        assertTrue(relativePath.startsWith("produtos"));
        assertTrue(relativePath.endsWith(".png"));
        Path saved = tempDir.resolve(relativePath);
        assertTrue(Files.exists(saved));
        assertEquals("conteudo", Files.readString(saved));
    }

    @Test
    void salvar_DeveNormalizarDestinationPath() throws IOException {
        String relativePath = adapter.salvar("abc".getBytes(StandardCharsets.UTF_8), "produtos/../produtos/11", "foto.jpg");

        assertTrue(relativePath.contains("produtos"));
        assertFalse(relativePath.contains(".."));
        assertTrue(Files.exists(tempDir.resolve(relativePath)));
    }

    @Test
    void carregarEDeletar_DeveFuncionar() throws IOException {
        String relativePath = adapter.salvar("xyz".getBytes(StandardCharsets.UTF_8), "outros", "arquivo");
        byte[] bytes = adapter.carregar(relativePath);
        assertEquals("xyz", new String(bytes, StandardCharsets.UTF_8));

        adapter.deletar(relativePath);
        assertFalse(Files.exists(tempDir.resolve(relativePath)));
    }
}

