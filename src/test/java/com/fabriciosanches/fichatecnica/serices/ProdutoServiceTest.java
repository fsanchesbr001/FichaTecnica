package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.dtos.ProdutoDTO;
import com.fabriciosanches.fichatecnica.domains.Produto;

import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import com.fabriciosanches.fichatecnica.services.ProdutoService;
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
    private ProdutoDTO produtoDTO;

    @BeforeEach
    void setUp() {
        produtoDTO = new ProdutoDTO(1L, "Produto Teste", "Descrição Teste",
                "teste1.jpg",new BigDecimal(BigInteger.TEN),
                new BigDecimal(BigInteger.TEN));
        produto = new Produto(produtoDTO);
    }

    @Test
    void listar_DeveRetornarListaDeProdutos() {
        when(repository.findAll()).thenReturn(List.of(produto));

        List<ProdutoDTO> result = service.listar();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Produto Teste", result.get(0).nome());
        verify(repository, times(1)).findAll();
    }





    @Test
    void cadastrarProduto_DeveSalvarNovoProduto() {
        when(repository.save(any(Produto.class))).thenReturn(produto);

        ProdutoDTO result = service.cadastrarProduto(produtoDTO);

        assertNotNull(result);
        assertEquals("Produto Teste", result.nome());
        verify(repository, times(1)).save(any(Produto.class));
    }

    @Test
    void atualizarProduto_DeveAtualizarProdutoExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(produto));
        when(repository.save(any(Produto.class))).thenReturn(produto);

        ProdutoDTO novosDados = new ProdutoDTO(1L, "Produto Atualizado", "Descrição Atualizada",
                "imagem2.jpg",new BigDecimal(5),new BigDecimal(5));

        ProdutoDTO result = service.atualizarProduto(1L, novosDados);

        assertNotNull(result);
        assertEquals("Produto Atualizado", result.nome());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(Produto.class));
    }


    @Test
    void deletarProduto_DeveDeletarProduto() {
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deletarProduto(1L));

        verify(repository, times(1)).deleteById(1L);
    }
}
