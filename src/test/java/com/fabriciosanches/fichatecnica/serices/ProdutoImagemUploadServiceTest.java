package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.domains.Produto;
import com.fabriciosanches.fichatecnica.dtos.UploadJobDTO;
import com.fabriciosanches.fichatecnica.enums.UploadJobStatus;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import com.fabriciosanches.fichatecnica.services.ProdutoImagemUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoImagemUploadServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoImagemUploadService service;

    private Produto produto;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        produto = new Produto(4L, "Bolo", "Descrição", null, new BigDecimal("15.00"), BigDecimal.ZERO, List.of());
        tempDir = Files.createTempDirectory("produto-upload-test");
        ReflectionTestUtils.setField(service, "storagePath", tempDir.toString());
        ReflectionTestUtils.setField(service, "publicUrl", "http://localhost:8080/uploads");
    }

    @Test
    void iniciarUpload_DeveProcessarArquivoESalvarUrlNoProduto() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "imagem.png",
                "image/png",
                "conteudo".getBytes(StandardCharsets.UTF_8)
        );
        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UploadJobDTO result = service.iniciarUpload(4L, file);

        assertNotNull(result.jobId());
        assertEquals(UploadJobStatus.DONE, result.status());
        assertEquals(4L, result.produtoId());
        assertNotNull(result.imagemUrl());
        assertTrue(result.imagemUrl().startsWith("http://localhost:8080/uploads/produtos/4/"));
        assertEquals(result.imagemUrl(), produto.getImagem());
        verify(produtoRepository, atLeastOnce()).save(produto);
    }

    @Test
    void iniciarUpload_DeveLancarExcecaoQuandoProdutoNaoExistir() {
        MockMultipartFile file = new MockMultipartFile("file", "imagem.png", "image/png", new byte[]{1});
        when(produtoRepository.findById(4L)).thenReturn(Optional.empty());

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.iniciarUpload(4L, file));

        assertEquals("Produto não encontrado id=4", exception.getMessage());
    }

    @Test
    void iniciarUpload_DeveLancarExcecaoQuandoArquivoVazio() {
        MockMultipartFile file = new MockMultipartFile("file", "imagem.png", "image/png", new byte[]{});
        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.iniciarUpload(4L, file));

        assertEquals("Arquivo de imagem não pode ser vazio.", exception.getMessage());
    }

    @Test
    void iniciarUpload_DeveLancarExcecaoQuandoArquivoExcederLimite() {
        MultipartFile file = mock(MultipartFile.class);
        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(10L * 1024 * 1024 + 1);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.iniciarUpload(4L, file));

        assertEquals("Arquivo excede o tamanho máximo permitido de 10 MB.", exception.getMessage());
    }

    @Test
    void iniciarUpload_DeveLancarExcecaoQuandoMimeForInvalido() {
        MockMultipartFile file = new MockMultipartFile("file", "imagem.gif", "image/gif", new byte[]{1});
        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.iniciarUpload(4L, file));

        assertEquals("Tipo de arquivo não suportado: image/gif. Permitidos: jpg, jpeg, png, webp.", exception.getMessage());
    }

    @Test
    void iniciarUpload_DeveLancarExcecaoQuandoExtensaoForInvalida() {
        MockMultipartFile file = new MockMultipartFile("file", "imagem.gif", "image/png", new byte[]{1});
        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.iniciarUpload(4L, file));

        assertEquals("Extensão não permitida: gif. Permitidas: jpg, jpeg, png, webp.", exception.getMessage());
    }

    @Test
    void consultarStatus_DeveLancarExcecaoQuandoJobNaoExistir() {
        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.consultarStatus("job-inexistente"));

        assertEquals("Job de upload não encontrado: job-inexistente", exception.getMessage());
    }

    @Test
    void processarUploadAsync_DeveMarcarJobComoErroQuandoFalharAoSalvarArquivo() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1L);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("imagem.png");
        when(file.getInputStream()).thenThrow(new IOException("falha-io"));

        UploadJobDTO result = service.iniciarUpload(4L, file);

        assertEquals(UploadJobStatus.ERROR, result.status());
        assertEquals("falha-io", result.message());
    }

    @Test
    void removerImagem_DeveApagarArquivoFisicoELimparCampo() throws IOException {
        Path produtoDir = Files.createDirectories(tempDir.resolve(Path.of("produtos", "4")));
        Path imageFile = produtoDir.resolve("foto.png");
        Files.writeString(imageFile, "conteudo");
        produto.setImagem("http://localhost:8080/uploads/produtos/4/foto.png");
        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));

        service.removerImagem(4L);

        assertNull(produto.getImagem());
        assertFalse(Files.exists(imageFile));
        verify(produtoRepository).save(produto);
    }

    @Test
    void removerImagem_DeveRetornarSemSalvarQuandoNaoHouverImagem() {
        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));

        assertDoesNotThrow(() -> service.removerImagem(4L));

        verify(produtoRepository, never()).save(any());
    }

    @Test
    void removerImagem_DeveLancarExcecaoQuandoProdutoNaoExistir() {
        when(produtoRepository.findById(4L)).thenReturn(Optional.empty());

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.removerImagem(4L));

        assertEquals("Produto não encontrado id=4", exception.getMessage());
    }

    @Test
    void listarJobsAtivos_DeveRetornarJobsRegistrados() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "imagem.png",
                "image/png",
                "conteudo".getBytes(StandardCharsets.UTF_8)
        );
        when(produtoRepository.findById(4L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.iniciarUpload(4L, file);

        List<UploadJobDTO> result = service.listarJobsAtivos();

        assertEquals(1, result.size());
        assertEquals(UploadJobStatus.DONE, result.get(0).status());
    }
}

