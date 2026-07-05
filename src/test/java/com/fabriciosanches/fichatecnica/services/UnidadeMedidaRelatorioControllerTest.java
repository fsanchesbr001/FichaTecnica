package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioDetalheUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioListaUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.UnidadeMedidaRelatorioController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UnidadeMedidaRelatorioControllerTest {

    private MockMvc mockMvc;
    private GerarRelatorioListaUnidadeMedidaPort gerarListaPort;
    private GerarRelatorioDetalheUnidadeMedidaPort gerarDetalhePort;

    @BeforeEach
    void setUp() {
        gerarListaPort = Mockito.mock(GerarRelatorioListaUnidadeMedidaPort.class);
        gerarDetalhePort = Mockito.mock(GerarRelatorioDetalheUnidadeMedidaPort.class);

        UnidadeMedidaRelatorioController controller = new UnidadeMedidaRelatorioController(gerarListaPort, gerarDetalhePort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void relatorioLista_DeveRetornarOkQuandoGeradoComSucesso() throws Exception {
        when(gerarListaPort.executar()).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/ficha-tecnica/unidades-medida/relatorios/lista"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "inline; filename=lista_unidades_medida.pdf"));
    }

    @Test
    void relatorioLista_DeveRetornarNotFoundQuandoSemDados() throws Exception {
        when(gerarListaPort.executar()).thenThrow(new java.util.NoSuchElementException("vazio"));

        mockMvc.perform(get("/ficha-tecnica/unidades-medida/relatorios/lista"))
                .andExpect(status().isNotFound());
    }

    @Test
    void relatorioDetalhe_DeveRetornarOkQuandoGeradoComSucesso() throws Exception {
        when(gerarDetalhePort.executar("kg")).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/ficha-tecnica/unidades-medida/relatorios/{sigla}/detalhe", "kg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "inline; filename=detalhe_unidade_kg.pdf"));
    }

    @Test
    void relatorioDetalhe_DeveRetornarBadRequestQuandoSiglaInvalida() throws Exception {
        when(gerarDetalhePort.executar(anyString())).thenThrow(new IllegalArgumentException("inválida"));

        mockMvc.perform(get("/ficha-tecnica/unidades-medida/relatorios/{sigla}/detalhe", " "))
                .andExpect(status().isBadRequest());
    }
}
