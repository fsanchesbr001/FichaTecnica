package com.fabriciosanches.fichatecnica;

import com.fabriciosanches.fichatecnica.dtos.ItemDTO;
import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.domains.UnidadeMedida;
import com.fabriciosanches.fichatecnica.repository.HistoricoItemRepository;
import com.fabriciosanches.fichatecnica.repository.ItemRepository;
import com.fabriciosanches.fichatecnica.services.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private HistoricoItemRepository historicoItemRepository;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListar() {
        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setCodigo(1L);
        unidadeMedida.setNome("Unidade");
        unidadeMedida.setSigla("UN");

        ItemDTO itemDTO = new ItemDTO(1L, "Item1", unidadeMedida, BigDecimal.TEN);
        when(itemRepository.findAll()).thenReturn(List.of(new Item(itemDTO)));

        List<ItemDTO> result = itemService.listar();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Item1", result.get(0).nome());
    }

    @Test
    void testBuscarPorId() {
        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setCodigo(1L);
        unidadeMedida.setNome("Unidade");
        unidadeMedida.setSigla("UN");

        ItemDTO itemDTO = new ItemDTO(1L, "Item1", unidadeMedida, BigDecimal.TEN);
        when(itemRepository.findAll()).thenReturn(List.of(new Item(itemDTO)));

        ItemDTO result = itemService.buscarPorId(1L);

        assertNotNull(result);
        assertEquals("Item1", result.nome());
    }

    @Test
    void testCadastrarItem() {
        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setCodigo(1L);
        unidadeMedida.setNome("Unidade");
        unidadeMedida.setSigla("UN");

        ItemDTO itemDTO = new ItemDTO(1L, "Item1", unidadeMedida, BigDecimal.TEN);
        Item item = new Item(itemDTO);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDTO result = itemService.cadastrarItem(itemDTO);

        assertNotNull(result);
        assertEquals("Item1", result.nome());
    }

    @Test
    void testAtualizarItem() {
        ItemDTO itemDTO = new ItemDTO(1L, "Item1", null, BigDecimal.TEN);
        Item item = new Item(itemDTO);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDTO novosDados = new ItemDTO(1L, "Item2", null, BigDecimal.ONE);
        ItemDTO result = itemService.atualizarItem(1L, novosDados);

        assertNotNull(result);
        assertEquals("Item2", result.nome());
    }

    @Test
    void testDeletarItem() {
        doNothing().when(historicoItemRepository).deleteByItemCodigo(1L);
        doNothing().when(itemRepository).deleteById(1L);

        assertDoesNotThrow(() -> itemService.deletarItem(1L));
        verify(historicoItemRepository, times(1)).deleteByItemCodigo(1L);
        verify(itemRepository, times(1)).deleteById(1L);
    }
}
