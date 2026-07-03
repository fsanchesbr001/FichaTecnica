package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.Produto;
import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;

    private Produto produto;
    private Produto produtoSecundario;
    private ProdutoDTO produtoDTO;

    @BeforeEach
    void setUp() {
        produtoDTO = new ProdutoDTO(1L, "Produto Teste", "Descrição Teste",
                "teste1.jpg", new BigDecimal(BigInteger.TEN), new BigDecimal(BigInteger.TEN));
        produto = new Produto(produtoDTO);
        produtoSecundario = new Produto(2L, "Açúcar", "Descrição Açúcar",
                "acucar.jpg", new BigDecimal("12.50"), BigDecimal.ZERO, List.of());
    }

    @Test
    void listar_DeveRetornarListaDeProdutosOrdenada() {
        when(repository.findAll()).thenReturn(List.of(produto, produtoSecundario));

        List<ProdutoDTO> result = service.listar();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Açúcar", result.get(0).nome());
        assertEquals("Produto Teste", result.get(1).nome());
        verify(repository).findAll();
    }

    @Test
    void buscarPorId_DeveRetornarProdutoQuandoEncontrado() {
        when(repository.findAll()).thenReturn(List.of(produtoSecundario, produto));

        ProdutoDTO result = service.buscarPorId(1L);

        assertEquals(1L, result.codigo());
        assertEquals("Produto Teste", result.nome());
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoNaoEncontrado() {
        when(repository.findAll()).thenReturn(List.of(produtoSecundario));

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.buscarPorId(99L));

        assertEquals("Produto não encontrada", exception.getMessage());
    }

    @Test
    void findByName_DeveDelegarParaRepository() {
        when(repository.countByName("Produto Teste")).thenReturn(3L);

        assertEquals(3L, service.findByName("Produto Teste"));
    }

    @Test
    void cadastrarProduto_DeveSalvarNovoProduto() {
        when(repository.countByName("Produto Teste")).thenReturn(0L);
        when(repository.save(any(Produto.class))).thenReturn(produto);

        ProdutoDTO result = service.cadastrarProduto(produtoDTO);

        assertNotNull(result);
        assertEquals("Produto Teste", result.nome());
        verify(repository).countByName("Produto Teste");
        verify(repository).save(any(Produto.class));
    }

    @Test
    void cadastrarProduto_DeveLancarExcecaoQuandoDuplicado() {
        when(repository.countByName("Produto Teste")).thenReturn(1L);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.cadastrarProduto(produtoDTO));

        assertEquals("Produto já cadastrado", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void cadastrarProduto_DeveLancarExcecaoQuandoProdutoForNulo() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> service.cadastrarProduto(null));

        assertEquals("Produto não pode ser nulo", exception.getMessage());
    }

    @Test
    void atualizarProduto_DeveAtualizarProdutoExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(produto));
        when(repository.save(any(Produto.class))).thenReturn(produto);

        ProdutoDTO novosDados = new ProdutoDTO(1L, "Produto Atualizado", "Descrição Atualizada",
                "imagem2.jpg", new BigDecimal(5), new BigDecimal(5));

        ProdutoDTO result = service.atualizarProduto(1L, novosDados);

        assertNotNull(result);
        assertEquals("Produto Atualizado", result.nome());
        verify(repository).findById(1L);
        verify(repository).save(any(Produto.class));
    }

    @Test
    void atualizarProduto_DeveLancarExcecaoQuandoNaoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ProdutoDTO novosDados = new ProdutoDTO(1L, "Produto Atualizado", "Descrição Atualizada",
                "imagem2.jpg", new BigDecimal(5), new BigDecimal(5));

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.atualizarProduto(1L, novosDados));

        assertEquals("Produto com ID 1 não encontrada", exception.getMessage());
    }

    @Test
    void deletarProduto_DeveDeletarProduto() {
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deletarProduto(1L));

        verify(repository).deleteById(1L);
    }
}
