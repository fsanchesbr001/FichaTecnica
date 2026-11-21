package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.ArquivoUpload;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoImagemStoragePort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UploadJobDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutoDTO;
import com.fabriciosanches.fichatecnica.core.domain.enums.UploadJobStatus;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.out.persistence.ProdutoImagemStorageAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoImagemUploadUseCaseTest {

    @Mock
    private ProdutoRepositoryPort produtoRepositoryPort;
    @Mock
    private ProdutoImagemStoragePort produtoImagemStoragePort;

    @InjectMocks
    private ProdutoImagemUploadUseCase useCase;

    @Test
    void iniciar_DeveSalvarImagemERegistrarJob() {
        Produto produto = new Produto(4L, "Bolo", "Desc", null, new BigDecimal("15.00"), BigDecimal.ZERO, List.of());
        ArquivoUpload file = new ArquivoUpload("imagem.png", "image/png", "conteudo".getBytes());
        when(produtoRepositoryPort.buscarPorId(4L)).thenReturn(Optional.of(produto));
        when(produtoImagemStoragePort.salvar(any(), any(), any(), any())).thenReturn("http://localhost/uploads/produtos/4/imagem.png");
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UploadJobDTO resultado = useCase.iniciar(4L, file);

        assertNotNull(resultado.jobId());
        assertEquals(UploadJobStatus.DONE, resultado.status());
        assertEquals("http://localhost/uploads/produtos/4/imagem.png", resultado.imagemUrl());
        verify(produtoRepositoryPort, atLeastOnce()).salvar(any(Produto.class));
    }

    @Test
    void iniciar_DeveLancarExcecaoQuandoArquivoForInvalido() {
        Produto produto = new Produto(4L, "Bolo", "Desc", null, new BigDecimal("15.00"), BigDecimal.ZERO, List.of());
        when(produtoRepositoryPort.buscarPorId(4L)).thenReturn(Optional.of(produto));

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> useCase.iniciar(4L, new ArquivoUpload("imagem.png", "image/png", new byte[]{})));

        assertEquals("Arquivo de imagem nao pode ser vazio.", exception.getMessage());
    }

    @Test
    void iniciar_DevePreservarImagemAnteriorQuandoStorageFalhar() {
        Produto produto = new Produto(4L, "Bolo", "Desc", "produtos/4/anterior.png",
                new BigDecimal("15.00"), BigDecimal.ZERO, List.of());
        when(produtoRepositoryPort.buscarPorId(4L)).thenReturn(Optional.of(produto));
        when(produtoImagemStoragePort.salvar(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("Permissao negada no storage"));

        UploadJobDTO resultado = useCase.iniciar(4L,
                new ArquivoUpload("prato1.png", "image/png", new byte[]{1, 2, 3}));

        assertEquals(UploadJobStatus.ERROR, resultado.status());
        assertEquals("Erro ao salvar imagem do produto.", resultado.message());
        assertNull(resultado.imagemUrl());
        assertEquals("produtos/4/anterior.png", produto.getImagem());
        assertEquals(UploadJobStatus.ERROR, useCase.consultar(resultado.jobId()).status());
        verify(produtoRepositoryPort, never()).salvar(any());
    }

    @Test
    void falhaNoUpload_NaoDeveDesfazerCadastroDoProduto() {
        var produtos = new ProdutoUseCase(produtoRepositoryPort);
        var dados = new ProdutoDTO(4L, "Bolo", "Desc", "prato1.png",
                new BigDecimal("15.00"), BigDecimal.ZERO);
        when(produtoRepositoryPort.contarPorNome("Bolo")).thenReturn(0L);
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProdutoDTO cadastrado = produtos.cadastrarProduto(dados);
        var captor = ArgumentCaptor.forClass(Produto.class);
        verify(produtoRepositoryPort).salvar(captor.capture());
        when(produtoRepositoryPort.buscarPorId(4L)).thenReturn(Optional.of(captor.getValue()));
        when(produtoImagemStoragePort.salvar(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("Storage indisponivel"));

        UploadJobDTO job = useCase.iniciar(4L,
                new ArquivoUpload("prato1.png", "image/png", new byte[]{1, 2, 3}));

        assertEquals(UploadJobStatus.ERROR, job.status());
        assertEquals(cadastrado, produtos.buscarPorId(4L));
        assertNull(cadastrado.imagem());
        verify(produtoRepositoryPort).salvar(any(Produto.class));
        verify(produtoRepositoryPort, never()).deletarPorId(any());
    }

    @Test
    void falhaNoUpload_NaoDeveImpedirAlteracaoDoProduto() {
        Produto produto = new Produto(4L, "Bolo", "Desc", "produtos/4/anterior.png",
                new BigDecimal("15.00"), BigDecimal.ZERO, List.of());
        when(produtoRepositoryPort.buscarPorId(4L)).thenReturn(Optional.of(produto));
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produtoImagemStoragePort.salvar(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("Storage indisponivel"));

        UploadJobDTO job = useCase.iniciar(4L,
                new ArquivoUpload("prato1.png", "image/png", new byte[]{1, 2, 3}));
        ProdutoDTO alterado = new ProdutoUseCase(produtoRepositoryPort).atualizarProduto(4L,
                new ProdutoDTO(4L, "Bolo atualizado", "Nova descricao", "prato1.png",
                        new BigDecimal("20.00"), BigDecimal.ONE));

        assertEquals(UploadJobStatus.ERROR, job.status());
        assertEquals("Bolo atualizado", alterado.nome());
        assertEquals("Nova descricao", alterado.descricao());
        assertEquals(new BigDecimal("20.00"), alterado.valorVenda());
        assertEquals(BigDecimal.ONE, alterado.valorItens());
        assertEquals("produtos/4/anterior.png", alterado.imagem());
        verify(produtoRepositoryPort).salvar(produto);
    }

    @Test
    void iniciar_DevePersistirCaminhoDoArquivoRealNaoNomeOriginal(@TempDir Path diretorio) throws Exception {
        Produto produto = new Produto(4L, "Bolo", "Desc", null,
                new BigDecimal("15.00"), BigDecimal.ZERO, List.of());
        when(produtoRepositoryPort.buscarPorId(4L)).thenReturn(Optional.of(produto));
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var storage = new ProdutoImagemStorageAdapter();
        ReflectionTestUtils.setField(storage, "storagePath", diretorio.toString());
        var upload = new ProdutoImagemUploadUseCase(produtoRepositoryPort, storage);
        byte[] conteudo = {1, 2, 3};

        UploadJobDTO resultado = upload.iniciar(4L, new ArquivoUpload("prato1.png", "image/png", conteudo));

        assertEquals(UploadJobStatus.DONE, resultado.status());
        assertEquals(resultado.imagemUrl(), produto.getImagem());
        assertArrayEquals(conteudo, Files.readAllBytes(diretorio.resolve(resultado.imagemUrl())));
        assertEquals(3, diretorio.relativize(diretorio.resolve(resultado.imagemUrl())).getNameCount());
        verify(produtoRepositoryPort).salvar(produto);
    }

    @Test
    void consultar_DeveLancarExcecaoQuandoJobNaoExistir() {
        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> useCase.consultar("job-inexistente"));

        assertEquals("Job de upload nao encontrado: job-inexistente", exception.getMessage());
    }

    @Test
    void remover_DeveLimparImagemDoProduto() {
        Produto produto = new Produto(4L, "Bolo", "Desc", "http://localhost/uploads/produtos/4/foto.png", new BigDecimal("15.00"), BigDecimal.ZERO, List.of());
        when(produtoRepositoryPort.buscarPorId(4L)).thenReturn(Optional.of(produto));
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.remover(4L);

        verify(produtoImagemStoragePort).remover("http://localhost/uploads/produtos/4/foto.png", 4L);
        verify(produtoRepositoryPort).salvar(any(Produto.class));
    }

    @Test
    void listar_DeveRetornarJobsRegistrados() {
        Produto produto = new Produto(4L, "Bolo", "Desc", null, new BigDecimal("15.00"), BigDecimal.ZERO, List.of());
        ArquivoUpload file = new ArquivoUpload("imagem.png", "image/png", "conteudo".getBytes());
        when(produtoRepositoryPort.buscarPorId(4L)).thenReturn(Optional.of(produto));
        when(produtoImagemStoragePort.salvar(any(), any(), any(), any())).thenReturn("http://localhost/uploads/produtos/4/imagem.png");
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.iniciar(4L, file);

        List<UploadJobDTO> jobs = useCase.listar();

        assertEquals(1, jobs.size());
        assertEquals(UploadJobStatus.DONE, jobs.get(0).status());
    }
}

