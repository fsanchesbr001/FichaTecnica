package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.ports.in.ObterValoresConversaoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RegistrarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ItemProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.core.domain.ItemProduto;
import com.fabriciosanches.fichatecnica.core.domain.ItemProdutoId;
import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarItemUseCaseTest {

    @Mock
    private ItemRepositoryPort itemRepositoryPort;
    @Mock
    private RegistrarHistoricoItemPort registrarHistoricoItemPort;
    @Mock
    private ItemProdutoRepositoryPort itemProdutoRepositoryPort;
    @Mock
    private ObterValoresConversaoPort obterValoresConversaoPort;
    @Mock
    private ProdutoRepositoryPort produtoRepositoryPort;

    @InjectMocks
    private AtualizarItemUseCase useCase;

    @Test
    void atualizar_DeveLancarExcecaoQuandoItemNaoExiste() {
        UnidadeMedida unidade = new UnidadeMedida(1L, "Quilo", "kg");
        when(itemRepositoryPort.buscarPorId(1L)).thenReturn(Optional.empty());

        FichaTecnicaException ex = assertThrows(
                FichaTecnicaException.class,
                () -> useCase.atualizar(1L, "Farinha", unidade, new BigDecimal("10.00"))
        );

        assertEquals("Item com ID 1 não encontrado", ex.getMessage());
    }

    @Test
    void atualizar_DeveSalvarERegistrarHistoricoQuandoSemProdutosAssociados() {
        UnidadeMedida unidade = new UnidadeMedida(1L, "Quilo", "kg");
        Item item = new Item(1L, "Farinha", unidade, new BigDecimal("7.50"));

        when(itemRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(item));
        when(itemRepositoryPort.salvar(any(Item.class))).thenReturn(item);
        when(itemProdutoRepositoryPort.buscarPorItemId(1L)).thenReturn(List.of());

        Item result = useCase.atualizar(1L, "Farinha Premium", unidade, new BigDecimal("9.00"));

        assertEquals("Farinha Premium", result.getNome());
        verify(registrarHistoricoItemPort).registrar(eq(1L), eq(new BigDecimal("9.00")), any(LocalDate.class));
        verify(produtoRepositoryPort, never()).salvar(any(Produto.class));
    }

    @Test
    void atualizar_DeveRecalcularProdutoQuandoExistemItensProduto() {
        UnidadeMedida unidadeBase = new UnidadeMedida(1L, "Quilo", "kg");
        Item item = new Item(1L, "Farinha", unidadeBase, new BigDecimal("7.50"));

        Produto produto = new Produto(10L, "Bolo", "Desc", null, new BigDecimal("20.00"), BigDecimal.ZERO, null);
        ItemProduto itemProduto = new ItemProduto(new ItemProdutoId(10L, 1L), item, produto, new com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida(2L, "Grama", "g"),
                2.0, new BigDecimal("5.00"));

        when(itemRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(item));
        when(itemRepositoryPort.salvar(any(Item.class))).thenReturn(item);
        when(itemProdutoRepositoryPort.buscarPorItemId(1L)).thenReturn(List.of(itemProduto));
        when(obterValoresConversaoPort.obterValoresConversao(any(Item.class), eq(2.0), eq(2L)))
                .thenReturn(new ConversaoValoresDTO(2.0, 2L, new BigDecimal("7.00")));
        when(itemProdutoRepositoryPort.salvar(any(ItemProduto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(produtoRepositoryPort.salvar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.atualizar(1L, "Farinha Nova", unidadeBase, new BigDecimal("8.90"));

        ArgumentCaptor<ItemProduto> captor = ArgumentCaptor.forClass(ItemProduto.class);
        verify(itemProdutoRepositoryPort).salvar(captor.capture());
        assertEquals(new BigDecimal("7.00"), captor.getValue().getValor());
        verify(produtoRepositoryPort).salvar(any(Produto.class));
    }
}


