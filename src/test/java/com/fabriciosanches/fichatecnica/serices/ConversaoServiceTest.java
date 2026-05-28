package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.domains.Conversao;
import com.fabriciosanches.fichatecnica.domains.Item;
import com.fabriciosanches.fichatecnica.domains.UnidadeMedida;
import com.fabriciosanches.fichatecnica.dtos.ConversaoDTO;
import com.fabriciosanches.fichatecnica.dtos.ConversaoRelatorioDTO;
import com.fabriciosanches.fichatecnica.dtos.ConversaoValoresDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ConversaoRepository;
import com.fabriciosanches.fichatecnica.services.ConversaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversaoServiceTest {

    @Mock
    private ConversaoRepository repository;

    @InjectMocks
    private ConversaoService service;

    private Conversao conversaoMultiplica;
    private Conversao conversaoDivide;
    private ConversaoDTO conversaoDTO;
    private ConversaoRelatorioDTO conversaoRelatorioDTO;
    private Item item;

    @BeforeEach
    void setUp() {
        conversaoDTO = new ConversaoDTO(1L, 2L, 3L, "MULTIPLICA", new BigDecimal("2.00"));
        conversaoMultiplica = new Conversao(conversaoDTO);
        conversaoDivide = new Conversao(2L, 2L, 3L, "DIVIDE", new BigDecimal("2.00"));
        conversaoRelatorioDTO = new ConversaoRelatorioDTO(1L, "Quilo", "Grama", "MULTIPLICA", new BigDecimal("1000.00"));
        item = new Item(10L, "Farinha", new UnidadeMedida(2L, "Quilo", "kg"), null, new BigDecimal("10.00"));
    }

    @Test
    void listar_DeveRetornarListaDeConversoesOrdenada() {
        when(repository.findAll()).thenReturn(List.of(
                new Conversao(3L, 5L, 1L, "MULTIPLICA", BigDecimal.ONE),
                conversaoMultiplica
        ));

        List<ConversaoDTO> result = service.listar();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).unidadeDe());
        verify(repository).findAll();
    }

    @Test
    void buscarPorId_DeveRetornarConversaoQuandoEncontrada() {
        when(repository.findById(1L)).thenReturn(Optional.of(conversaoMultiplica));

        ConversaoDTO result = service.buscarPorId(1L);

        assertNotNull(result);
        assertEquals(1L, result.codigo());
        verify(repository).findById(1L);
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoNaoEncontrada() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class, () -> service.buscarPorId(1L));

        assertEquals("Conversão com ID 1 não encontrada", exception.getMessage());
    }

    @Test
    void listarParaPdf_DeveRetornarDadosProjetados() {
        when(repository.findAllComNomes()).thenReturn(List.of(conversaoRelatorioDTO));

        List<ConversaoRelatorioDTO> result = service.listarParaPdf();

        assertEquals(1, result.size());
        assertEquals("Quilo", result.get(0).unidadeDe());
    }

    @Test
    void buscarPorIdParaPdf_DeveRetornarProjecaoQuandoEncontrada() {
        when(repository.findByIdComNomes(1L)).thenReturn(Optional.of(conversaoRelatorioDTO));

        ConversaoRelatorioDTO result = service.buscarPorIdParaPdf(1L);

        assertEquals(1L, result.codigo());
        assertEquals("Grama", result.unidadePara());
    }

    @Test
    void buscarPorIdParaPdf_DeveLancarExcecaoQuandoNaoEncontrada() {
        when(repository.findByIdComNomes(1L)).thenReturn(Optional.empty());

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.buscarPorIdParaPdf(1L));

        assertEquals("Conversão com ID 1 não encontrada", exception.getMessage());
    }

    @Test
    void findByConversaoDePara_DeveDelegarParaRepository() {
        when(repository.countByUnidadeDeAndUnidadePara(2L, 3L)).thenReturn(4L);

        assertEquals(4L, service.findByConversaoDePara(2L, 3L));
    }

    @Test
    void cadastrarConversao_DeveSalvarNovaConversao() {
        when(repository.countByUnidadeDeAndUnidadePara(2L, 3L)).thenReturn(0L);
        when(repository.save(any(Conversao.class))).thenReturn(conversaoMultiplica);

        ConversaoDTO result = service.cadastrarConversao(conversaoDTO);

        assertNotNull(result);
        assertEquals(1L, result.codigo());
        verify(repository).save(any(Conversao.class));
    }

    @Test
    void cadastrarConversao_DeveLancarExcecaoQuandoDuplicada() {
        when(repository.countByUnidadeDeAndUnidadePara(2L, 3L)).thenReturn(1L);

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.cadastrarConversao(conversaoDTO));

        assertEquals("Conversão já cadastrada", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void cadastrarConversao_DeveLancarExcecaoQuandoNula() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> service.cadastrarConversao(null));

        assertEquals("Conversão não pode ser nula", exception.getMessage());
    }

    @Test
    void atualizarConversao_DeveAtualizarConversaoExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(conversaoMultiplica));
        when(repository.save(any(Conversao.class))).thenReturn(conversaoMultiplica);

        ConversaoDTO novosDados = new ConversaoDTO(1L, 3L, 4L, "DIVIDE", new BigDecimal("5.00"));
        ConversaoDTO result = service.atualizarConversao(1L, novosDados);

        assertNotNull(result);
        assertEquals(3L, result.unidadeDe());
        assertEquals(4L, result.unidadePara());
        assertEquals("DIVIDE", result.operacao());
    }

    @Test
    void atualizarConversao_DeveLancarExcecaoQuandoNaoEncontrada() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.atualizarConversao(1L, conversaoDTO));

        assertEquals("Conversão com ID 1 não encontrada", exception.getMessage());
    }

    @Test
    void deletarConversao_DeveDeletarConversao() {
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deletarConversao(1L));

        verify(repository).deleteById(1L);
    }

    @Test
    void obterValoresConversao_DeveCalcularValorMultiplicando() {
        when(repository.findByUnidadeDeAndUnidadePara(2L, 3L)).thenReturn(conversaoMultiplica);

        ConversaoValoresDTO result = service.obterValoresConversao(item, 3.0, 3L);

        assertEquals(3.0, result.quantidade());
        assertEquals(3L, result.unidadeMedidaPara());
        assertEquals(0, result.valor().compareTo(new BigDecimal("60.0000")));
    }

    @Test
    void obterValoresConversao_DeveCalcularValorDividindo() {
        when(repository.findByUnidadeDeAndUnidadePara(2L, 3L)).thenReturn(conversaoDivide);

        ConversaoValoresDTO result = service.obterValoresConversao(item, 4.0, 3L);

        assertEquals(0, result.valor().compareTo(new BigDecimal("20.0000")));
    }

    @Test
    void obterValoresConversao_DeveLancarExcecaoQuandoQuantidadeInvalida() {
        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.obterValoresConversao(item, 0.0, 3L));

        assertEquals("Quantidade deve ser maior que zero", exception.getMessage());
    }

    @Test
    void obterValoresConversao_DeveLancarExcecaoQuandoOperacaoForInvalida() {
        when(repository.findByUnidadeDeAndUnidadePara(2L, 3L))
                .thenReturn(new Conversao(3L, 2L, 3L, "SUBTRAI", BigDecimal.ONE));

        FichaTecnicaException exception = assertThrows(FichaTecnicaException.class,
                () -> service.obterValoresConversao(item, 1.0, 3L));

        assertEquals("Operação inválida", exception.getMessage());
    }
}
