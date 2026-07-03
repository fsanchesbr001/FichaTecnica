package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.UnidadeMedidaController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

class MedidasControllerTest {

    private MockMvc mockMvc;
    private BuscarUnidadeMedidaPort buscarUnidadeMedidaPort;
    private CriarUnidadeMedidaPort criarUnidadeMedidaPort;
    private AtualizarUnidadeMedidaPort atualizarUnidadeMedidaPort;
    private DeletarUnidadeMedidaPort deletarUnidadeMedidaPort;

    @BeforeEach
    void setUp() {
        buscarUnidadeMedidaPort = Mockito.mock(BuscarUnidadeMedidaPort.class);
        criarUnidadeMedidaPort = Mockito.mock(CriarUnidadeMedidaPort.class);
        atualizarUnidadeMedidaPort = Mockito.mock(AtualizarUnidadeMedidaPort.class);
        deletarUnidadeMedidaPort = Mockito.mock(DeletarUnidadeMedidaPort.class);

        UnidadeMedidaController controller = new UnidadeMedidaController(
                buscarUnidadeMedidaPort,
                criarUnidadeMedidaPort,
                atualizarUnidadeMedidaPort,
                deletarUnidadeMedidaPort
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscarLista_DeveRetornarOk() throws Exception {
        when(buscarUnidadeMedidaPort.buscarTodos())
                .thenReturn(List.of(new UnidadeMedida(1L, "Quilograma", "KG")));

        mockMvc.perform(get("/ficha-tecnica/unidades-medida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Quilograma"));
    }

    @Test
    void buscarPorId_DeveRetornarNotFoundQuandoNaoEncontrar() throws Exception {
        when(buscarUnidadeMedidaPort.buscarPorId(1L))
                .thenThrow(new java.util.NoSuchElementException("nao encontrado"));

        mockMvc.perform(get("/ficha-tecnica/unidades-medida/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void cadastrarUnidade_DeveRetornarOk() throws Exception {
        when(criarUnidadeMedidaPort.criar(eq("Quilograma"), eq("kg")))
                .thenReturn(new UnidadeMedida(1L, "Quilograma", "KG"));

        mockMvc.perform(post("/ficha-tecnica/unidades-medida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Quilograma",
                                  "sigla": "kg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(1))
                .andExpect(jsonPath("$.sigla").value("KG"));
    }

    @Test
    void atualizarUnidade_DeveRetornarBadRequestQuandoInvalida() throws Exception {
        when(atualizarUnidadeMedidaPort.atualizar(eq(1L), any(), any()))
                .thenThrow(new IllegalArgumentException("Sigla não pode ser vazia"));

        mockMvc.perform(put("/ficha-tecnica/unidades-medida/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Quilograma",
                                  "sigla": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Sigla não pode ser vazia"));
    }

    @Test
    void apagar_DeveRetornarConflictComMensagem() throws Exception {
        doThrow(new IllegalStateException("unidade em uso")).when(deletarUnidadeMedidaPort).deletar(1L);

        mockMvc.perform(delete("/ficha-tecnica/unidades-medida/{id}", 1L))
                .andExpect(status().isConflict())
                .andExpect(content().string("unidade em uso"));
    }

    @Test
    void cadastrarUnidade_DeveRetornarBadRequestQuandoCasoDeUsoFalhar() throws Exception {
        when(criarUnidadeMedidaPort.criar(any(), any()))
                .thenThrow(new IllegalArgumentException("Sigla não pode ser vazia"));

        mockMvc.perform(post("/ficha-tecnica/unidades-medida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Quilograma",
                                  "sigla": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Sigla não pode ser vazia"));
    }
}
