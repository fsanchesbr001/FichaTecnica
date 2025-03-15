package com.fabriciosanches.fichatecnica;

import com.fabriciosanches.fichatecnica.controllers.MedidasController;
import com.fabriciosanches.fichatecnica.dtos.UnidadeMedidaDTO;
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
        UnidadeMedidaDTO unidade1 = new UnidadeMedidaDTO(1L, "Unidade A", "UA");
        UnidadeMedidaDTO unidade2 = new UnidadeMedidaDTO(2L, "Unidade B", "UB");
        when(service.listar()).thenReturn(List.of(unidade1, unidade2));

        ResponseEntity<List<UnidadeMedidaDTO>> response = controller.buscarLista();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void buscarPorId_DeveRetornarUnidadeQuandoEncontrada() {
        UnidadeMedidaDTO unidade = new UnidadeMedidaDTO(1L, "Unidade A", "UA");
        when(service.buscarPorId(1L)).thenReturn(unidade);

        ResponseEntity<UnidadeMedidaDTO> response = controller.buscarPorId(1L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Unidade A", response.getBody().nome());
    }

    @Test
    void buscarPorId_DeveRetornarNotFoundQuandoNaoEncontrada() {
        when(service.buscarPorId(1L)).thenThrow(new FichaTecnicaException("Unidade de medida não encontrada"));

        ResponseEntity<UnidadeMedidaDTO> response = controller.buscarPorId(1L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void cadastrarUnidade_DeveCadastrarUnidade() {
        UnidadeMedidaDTO dados = new UnidadeMedidaDTO(1L, "Unidade A", "UA");
        when(service.cadastrarUnidade(any(UnidadeMedidaDTO.class)))
                .thenReturn(new UnidadeMedidaDTO(1L, "Unidade A", "UA"));

        ResponseEntity<UnidadeMedidaDTO> response = controller.cadastrarUnidade(dados);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Unidade A", response.getBody().nome());
    }

    @Test
    void atualizarUnidade_DeveAtualizarUnidadeExistente() {
        UnidadeMedidaDTO dados = new UnidadeMedidaDTO(1L, "Unidade Atualizada", "UA");
        when(service.atualizarUnidade(eq(1L), any(UnidadeMedidaDTO.class)))
                .thenReturn(new UnidadeMedidaDTO(1L, "Unidade Atualizada", "UA"));

        ResponseEntity<UnidadeMedidaDTO> response = controller.atualizarUnidade(1L, dados);

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
