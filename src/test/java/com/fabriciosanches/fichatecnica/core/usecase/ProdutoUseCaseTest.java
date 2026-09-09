package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoUseCaseTest {

    @Mock
    private ProdutoRepositoryPort produtoRepositoryPort;

    @InjectMocks
    private ProdutoUseCase useCase;

    private Produto produtoA;
    private Produto produtoB;

    @BeforeEach
    void setUp() {
        produtoA = new Produto(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO, List.of());
        produtoB = new Produto(2L, "Torta", "Desc2", null, new BigDecimal("25.00"), BigDecimal.ZERO, List.of());
    }

    @Test
    void listar_DeveRetornarOrdenadoPorNome() {
        when(produtoRepositoryPort.buscarTodos()).thenReturn(List.of(produtoB, produtoA));

        List<ProdutoDTO> resultado = useCase.listar();

        assertEquals(2, resultado.size());
        assertEquals("Bolo", resultado.get(0).nome());
        assertEquals("Torta", resultado.get(1).nome());
    }

    @Test
    void buscarPorId_DeveRetornarProdutoQuandoExistir() {
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produtoA));

        ProdutoDTO resultado = useCase.buscarPorId(1L);

        assertEquals(1L, resultado.codigo());
        assertEquals("Bolo", resultado.nome());
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoNaoExistir() {
        when(produtoRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class, () -> useCase.buscarPorId(99L));

        assertEquals("Produto não encontrada", exception.getMessage());
    }

    @Test
    void cadastrarProduto_DeveSalvarNovoProduto() {
        ProdutoDTO produtoDTO = new ProdutoDTO(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO);
        when(produtoRepositoryPort.contarPorNome("Bolo")).thenReturn(0L);
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenReturn(produtoA);

        ProdutoDTO resultado = useCase.cadastrarProduto(produtoDTO);

        assertEquals("Bolo", resultado.nome());
        verify(produtoRepositoryPort).salvar(any(Produto.class));
    }

    @Test
    void cadastrarProduto_DeveLancarExcecaoQuandoDuplicado() {
        ProdutoDTO produtoDTO = new ProdutoDTO(1L, "Bolo", "Desc", null, new BigDecimal("19.90"), BigDecimal.ZERO);
        when(produtoRepositoryPort.contarPorNome("Bolo")).thenReturn(1L);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class, () -> useCase.cadastrarProduto(produtoDTO));

        assertEquals("Produto já cadastrado", exception.getMessage());
        verify(produtoRepositoryPort, never()).salvar(any());
    }

    @Test
    void atualizarProduto_DeveAtualizarESalvar() {
        ProdutoDTO novosDados = new ProdutoDTO(1L, "Bolo Novo", "Nova Desc", "img.jpg", new BigDecimal("21.90"), new BigDecimal("1.00"));
        when(produtoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produtoA));
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProdutoDTO resultado = useCase.atualizarProduto(1L, novosDados);

        assertEquals("Bolo Novo", resultado.nome());
        assertEquals("Nova Desc", resultado.descricao());
        verify(produtoRepositoryPort).salvar(any(Produto.class));
    }

    @Test
    void deletar_DeveDelegarParaRepository() {
        useCase.deletar(1L);
        verify(produtoRepositoryPort).deletarPorId(1L);
    }
}

