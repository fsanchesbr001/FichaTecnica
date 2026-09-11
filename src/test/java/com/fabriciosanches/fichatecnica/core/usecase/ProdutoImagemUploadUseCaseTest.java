package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.ArquivoUpload;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoImagemStoragePort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.dtos.UploadJobDTO;
import com.fabriciosanches.fichatecnica.enums.UploadJobStatus;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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

        assertEquals("Arquivo de imagem não pode ser vazio.", exception.getMessage());
    }

    @Test
    void consultar_DeveLancarExcecaoQuandoJobNaoExistir() {
        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> useCase.consultar("job-inexistente"));

        assertEquals("Job de upload não encontrado: job-inexistente", exception.getMessage());
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
