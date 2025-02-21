package com.fabriciosanches.fichatecnica;

import com.fabriciosanches.fichatecnica.controller.MedidasController;
import com.fabriciosanches.fichatecnica.domain.medidas.DadosUnidadeMedida;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.UnidadeMedidaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedidasControllerTest {

    @Mock
    private UnidadeMedidaService service;

    @InjectMocks
    private MedidasController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buscarLista_DeveRetornarListaDeUnidades() {
        DadosUnidadeMedida unidade1 = new DadosUnidadeMedida(1L, "Unidade A", "UA");
        DadosUnidadeMedida unidade2 = new DadosUnidadeMedida(2L, "Unidade B", "UB");
        when(service.listar()).thenReturn(List.of(unidade1, unidade2));

        ResponseEntity<List<DadosUnidadeMedida>> response = controller.buscarLista();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void buscarPorId_DeveRetornarUnidadeQuandoEncontrada() {
        DadosUnidadeMedida unidade = new DadosUnidadeMedida(1L, "Unidade A", "UA");
        when(service.buscarPorId(1L)).thenReturn(unidade);

        ResponseEntity<DadosUnidadeMedida> response = controller.buscarPorId(1L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Unidade A", response.getBody().nome());
    }

    @Test
    void buscarPorId_DeveRetornarNotFoundQuandoNaoEncontrada() {
        when(service.buscarPorId(1L)).thenThrow(new FichaTecnicaException("Unidade de medida não encontrada"));

        ResponseEntity<DadosUnidadeMedida> response = controller.buscarPorId(1L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void cadastrarUnidade_DeveCadastrarUnidade() {
        DadosUnidadeMedida dados = new DadosUnidadeMedida(1L, "Unidade A", "UA");
        when(service.cadastrarUnidade(any(DadosUnidadeMedida.class))).thenReturn(dados);

        ResponseEntity<DadosUnidadeMedida> response = controller.cadastrarUnidade(dados);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Unidade A", response.getBody().nome());
    }

    @Test
    void atualizarUnidade_DeveAtualizarUnidadeExistente() {
        DadosUnidadeMedida dados = new DadosUnidadeMedida(1L, "Unidade Atualizada", "UA");
        when(service.atualizarUnidade(eq(1L), any(DadosUnidadeMedida.class))).thenReturn(dados);

        ResponseEntity<DadosUnidadeMedida> response = controller.atualizarUnidade(1L, dados);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Unidade Atualizada", response.getBody().nome());
    }

    @Test
    void apagar_DeveDeletarUnidade() {
        doNothing().when(service).deletarUnidade(1L);

        ResponseEntity<Void> response = controller.apagar(1L);

        assertEquals(204, response.getStatusCode().value());
        verify(service, times(1)).deletarUnidade(1L);
    }
}
