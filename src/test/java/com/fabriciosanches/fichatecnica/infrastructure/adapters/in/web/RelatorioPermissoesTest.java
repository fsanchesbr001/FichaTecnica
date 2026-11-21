package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.Item;
import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.domain.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.core.domain.enums.TipoRelatorio;
import com.fabriciosanches.fichatecnica.core.domain.enums.UserRole;
import com.fabriciosanches.fichatecnica.core.ports.in.*;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ConversaoRelatorioDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.ProdutoDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RegisterDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RelatorioRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(RelatorioPermissoesTest.Config.class)
class RelatorioPermissoesTest {

    private static final byte[] PDF = {1, 2, 3};

    @Autowired private ItemController itens;
    @Autowired private ProdutoController produtos;
    @Autowired private ConversaoController conversoes;
    @Autowired private RelatorioController relatorios;
    @Autowired private UnidadeMedidaRelatorioController unidades;
    @Autowired private UsuarioController usuarios;

    @WithMockUser(roles = "USER")
    @ParameterizedTest
    @EnumSource(Impressao.class)
    void userPodeImprimirListasEDetalhes(Impressao impressao) {
        assertPdf(imprimir(impressao));
    }

    @WithMockUser(roles = "ADMIN")
    @ParameterizedTest
    @EnumSource(Impressao.class)
    void adminContinuaPodendoImprimir(Impressao impressao) {
        assertPdf(imprimir(impressao));
    }

    @WithMockUser(roles = "SYSTEM")
    @ParameterizedTest
    @EnumSource(value = Impressao.class, mode = EnumSource.Mode.EXCLUDE,
            names = {"UNIDADES_LISTA", "UNIDADES_DETALHE"})
    void naoAmpliaPermissoesDeSystem(Impressao impressao) {
        assertThatThrownBy(() -> imprimir(impressao)).isInstanceOf(AccessDeniedException.class);
    }

    @WithAnonymousUser
    @ParameterizedTest
    @EnumSource(value = Impressao.class, mode = EnumSource.Mode.EXCLUDE,
            names = {"UNIDADES_LISTA", "UNIDADES_DETALHE"})
    void impressoesContinuamExigindoAutenticacao(Impressao impressao) {
        assertThatThrownBy(() -> imprimir(impressao)).isInstanceOf(AccessDeniedException.class);
    }

    @WithMockUser(roles = "USER")
    @Test
    void userContinuaSemAcessoAListaEGestaoDeUsuarios() {
        assertThatThrownBy(() -> usuarios.listarTodosUsuarios()).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> usuarios.registrarUsuario(
                new RegisterDTO("test@example.com", null, UserRole.USER, "Teste", "52998224725")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void adminContinuaComAcessoAListaDeUsuarios() {
        assertThat(usuarios.listarTodosUsuarios().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private ResponseEntity<byte[]> imprimir(Impressao impressao) {
        return switch (impressao) {
            case ITENS_LISTA -> itens.gerarPdfLista();
            case ITENS_DETALHE -> itens.gerarPdfDetalhe(1L);
            case PRODUTOS_LISTA -> produtos.gerarPdfLista();
            case PRODUTOS_DETALHE -> produtos.gerarPdfDetalhe(1L);
            case CONVERSOES_LISTA -> conversoes.gerarPdfLista();
            case CONVERSOES_DETALHE -> conversoes.gerarPdfDetalhe(1L);
            case GENERICO -> relatorios.gerarPDF(new RelatorioRequestDTO("[{\"nome\":\"Teste\"}]", "",
                    "Lista", Map.of("nome", "Nome"), TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false));
            case UNIDADES_LISTA -> unidades.relatorioLista();
            case UNIDADES_DETALHE -> unidades.relatorioDetalhe("kg");
        };
    }

    private void assertPdf(ResponseEntity<byte[]> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getBody()).isEqualTo(PDF);
    }

    enum Impressao {
        ITENS_LISTA, ITENS_DETALHE, PRODUTOS_LISTA, PRODUTOS_DETALHE,
        CONVERSOES_LISTA, CONVERSOES_DETALHE, GENERICO, UNIDADES_LISTA, UNIDADES_DETALHE
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class Config {

        @Bean
        GerarRelatorioPort gerarRelatorioPermissoesPort() throws IOException {
            var port = mock(GerarRelatorioPort.class);
            when(port.gerarRelatorioPDF(any())).thenReturn(PDF);
            return port;
        }

        @Bean
        ItemController itemController(@Qualifier("gerarRelatorioPermissoesPort") GerarRelatorioPort relatorio) {
            var busca = mock(BuscarItemPort.class);
            var item = new Item(1L, "Farinha", new UnidadeMedida(1L, "Quilo", "kg"), new BigDecimal("7.50"));
            when(busca.listar()).thenReturn(List.of(item));
            when(busca.buscarPorId(1L)).thenReturn(item);
            return new ItemController(busca, mock(CriarItemPort.class), mock(AtualizarItemPort.class),
                    mock(DeletarItemPort.class), mock(ListarHistoricoItemPort.class), relatorio,
                    mock(GerarGraficoPort.class));
        }

        @Bean
        ProdutoController produtoController(@Qualifier("gerarRelatorioPermissoesPort") GerarRelatorioPort relatorio) {
            var busca = mock(BuscarProdutoPort.class);
            var produto = new ProdutoDTO(1L, "Bolo", "Chocolate", null,
                    new BigDecimal("10.00"), new BigDecimal("7.00"));
            when(busca.listar()).thenReturn(List.of(produto));
            when(busca.buscarPorId(1L)).thenReturn(produto);
            return new ProdutoController(busca, mock(CriarProdutoPort.class), mock(AtualizarProdutoPort.class),
                    mock(DeletarProdutoPort.class), mock(ListarItensDoProdutoPort.class),
                    mock(ObterDescricoesUnidadePort.class), mock(GerarGraficoPizzaProdutoPort.class),
                    relatorio, mock(GerarGraficoPort.class));
        }

        @Bean
        ConversaoController conversaoController(@Qualifier("gerarRelatorioPermissoesPort") GerarRelatorioPort relatorio) {
            var busca = mock(GerarRelatorioConversaoPort.class);
            var conversao = new ConversaoRelatorioDTO(1L, "kg", "g", "*", new BigDecimal("1000.00"));
            when(busca.buscarTodosComNomes()).thenReturn(List.of(conversao));
            when(busca.buscarPorIdComNomes(1L)).thenReturn(conversao);
            return new ConversaoController(mock(BuscarConversaoPort.class), mock(CriarConversaoPort.class),
                    mock(AtualizarConversaoPort.class), mock(DeletarConversaoPort.class), busca, relatorio);
        }

        @Bean
        RelatorioController relatorioController(@Qualifier("gerarRelatorioPermissoesPort") GerarRelatorioPort relatorio) {
            return new RelatorioController(relatorio, mock(GerarGraficoPort.class));
        }

        @Bean
        UnidadeMedidaRelatorioController unidadeMedidaRelatorioController() {
            var lista = mock(GerarRelatorioListaUnidadeMedidaPort.class);
            var detalhe = mock(GerarRelatorioDetalheUnidadeMedidaPort.class);
            when(lista.executar()).thenReturn(PDF);
            when(detalhe.executar("kg")).thenReturn(PDF);
            return new UnidadeMedidaRelatorioController(lista, detalhe);
        }

        @Bean
        UsuarioController usuarioController() {
            return new UsuarioController(mock(CriarUsuarioPort.class), mock(ExcluirUsuarioPort.class),
                    mock(PrimeiroAcessoPort.class), mock(GerenciarBloqueioPort.class),
                    mock(AtualizarUsuarioPort.class), mock(BuscarUsuarioPort.class), mock(ControleAcessoPort.class));
        }
    }
}
