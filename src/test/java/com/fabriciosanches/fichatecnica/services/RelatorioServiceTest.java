package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.enums.ImagemPosicao;
import com.fabriciosanches.fichatecnica.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.enums.TipoRelatorio;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RelatorioServiceTest {

    private final RelatorioService service = new RelatorioService();

    @Test
    void gerarRelatorioPDF_DeveGerarPdfListaComCamposFormatados() throws IOException {
        String json = """
                [
                  {
                    "nome": "Fabricio",
                    "cpf": "12345678901",
                    "ativo": true,
                    "dataCriacao": "2026-03-21T10:15:30",
                    "unidadeMedida": {"nome":"Quilo","sigla":"kg"}
                  }
                ]
                """;
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");
        colunas.put("cpf", "CPF");
        colunas.put("ativo", "Ativo");
        colunas.put("dataCriacao", "Data");
        colunas.put("unidadeMedida", "Unidade");

        byte[] pdf = service.gerarRelatorioPDF(new RelatorioRequestDTO(
                json, "", "Lista de Usuários", colunas,
                TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, true
        ));

        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        assertEquals('%', pdf[0]);
        assertEquals('P', pdf[1]);
        assertEquals('D', pdf[2]);
        assertEquals('F', pdf[3]);
    }

    @Test
    void gerarRelatorioPDF_DeveGerarPdfDetalheComOrientacaoForcadaEPermitirImagem() throws IOException {
        String json = """
                [
                  {
                    "nome": "Luciana",
                    "email": "luciana@email.com",
                    "cpf": "98765432100",
                    "ativo": false
                  }
                ]
                """;
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");
        colunas.put("email", "Email");
        colunas.put("cpf", "CPF");
        colunas.put("ativo", "Ativo");

        byte[] pdf = service.gerarRelatorioPDF(new RelatorioRequestDTO(
                json, "", "Detalhe do Usuário", colunas,
                TipoRelatorio.DETALHE, OrientacaoRelatorio.RETRATO, false,
                true, gerarImagemValida(), ImagemPosicao.FIM
        ));

        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        assertEquals('%', pdf[0]);
    }

    @Test
    void gerarRelatorioPDF_DeveGerarPdfListaComImagemNoInicio() throws IOException {
        String json = "[{\"nome\":\"Produto A\",\"ativo\":true}]";
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");
        colunas.put("ativo", "Ativo");

        byte[] pdf = service.gerarRelatorioPDF(new RelatorioRequestDTO(
                json, "", "Lista com Imagem", colunas,
                TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false,
                true, gerarImagemValida(), ImagemPosicao.INICIO
        ));

        assertTrue(pdf.length > 500);
    }

    @Test
    void gerarRelatorioPDF_DeveGerarPdfDetalheSemColunasUsandoCamposDoJson() throws IOException {
        String json = """
                [
                  {"nome":"Produto A","sigla":"kg","ativo":false},
                  {"nome":"Produto B","sigla":"g","ativo":true}
                ]
                """;

        byte[] pdf = service.gerarRelatorioPDF(new RelatorioRequestDTO(
                json, "", "Detalhe sem Colunas", new LinkedHashMap<>(),
                TipoRelatorio.DETALHE, OrientacaoRelatorio.RETRATO, false
        ));

        assertTrue(pdf.length > 500);
    }

    @Test
    void gerarRelatorioPDF_DeveUsarListPathQuandoJsonForAninhado() throws IOException {
        String json = """
                {
                  "dados": {
                    "lista": [
                      {"nome": "Produto A", "ativo": true}
                    ]
                  }
                }
                """;
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");
        colunas.put("ativo", "Ativo");

        byte[] pdf = service.gerarRelatorioPDF(new RelatorioRequestDTO(
                json, "dados.lista", "Lista", colunas,
                TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false
        ));

        assertTrue(pdf.length > 500);
    }

    @Test
    void gerarRelatorioPDF_DeveLancarExcecaoQuandoNaoHouverRegistros() {
        String json = "[]";
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.gerarRelatorioPDF(new RelatorioRequestDTO(
                        json, "", "Lista Vazia", colunas,
                        TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false
                )));

        assertEquals("Nenhum registro encontrado para gerar o relatório.", exception.getMessage());
    }

    @Test
    void gerarRelatorioPDF_DeveLancarExcecaoQuandoJsonRaizNaoForArrayESemListPath() {
        String json = "{\"nome\":\"Teste\"}";
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.gerarRelatorioPDF(new RelatorioRequestDTO(
                        json, "", "Raiz Inválida", colunas,
                        TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false
                )));

        assertEquals("O JSON raiz não é um array e nenhum listPath foi informado.", exception.getMessage());
    }

    @Test
    void gerarRelatorioPDF_DeveLancarExcecaoQuandoListaNaoTiverColunas() {
        String json = "[{\"nome\":\"Teste\"}]";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.gerarRelatorioPDF(new RelatorioRequestDTO(
                        json, "", "Sem Colunas", new LinkedHashMap<>(),
                        TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false
                )));

        assertEquals("Para LISTA, o mapa de colunas deve ser informado.", exception.getMessage());
    }

    @Test
    void gerarRelatorioPDF_DeveLancarExcecaoQuandoAlternarCoresForUsadoEmDetalhe() {
        String json = "[{\"nome\":\"Teste\"}]";
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.gerarRelatorioPDF(new RelatorioRequestDTO(
                        json, "", "Detalhe", colunas,
                        TipoRelatorio.DETALHE, OrientacaoRelatorio.RETRATO, true
                )));

        assertEquals("AlternarCores só pode ser true quando tipoRelatorio = LISTA.", exception.getMessage());
    }

    @Test
    void gerarRelatorioPDF_DeveLancarExcecaoQuandoUsarImagemSemBytes() {
        String json = "[{\"nome\":\"Teste\"}]";
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.gerarRelatorioPDF(new RelatorioRequestDTO(
                        json, "", "Lista", colunas,
                        TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false,
                        true, null, ImagemPosicao.INICIO
                )));

        assertEquals("usarImagem=true exige que o campo 'imagem' seja informado.", exception.getMessage());
    }

    @Test
    void gerarRelatorioPDF_DeveLancarExcecaoQuandoUsarImagemSemPosicao() {
        String json = "[{\"nome\":\"Teste\"}]";
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.gerarRelatorioPDF(new RelatorioRequestDTO(
                        json, "", "Lista", colunas,
                        TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false,
                        true, gerarImagemValida(), null
                )));

        assertEquals("usarImagem=true exige que o campo 'imagemPosicao' seja informado (INICIO ou FIM).", exception.getMessage());
    }

    @Test
    void gerarRelatorioPDF_DeveLancarExcecaoQuandoListPathForInvalido() {
        String json = "{\"dados\":{\"itens\":[]}}";
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.gerarRelatorioPDF(new RelatorioRequestDTO(
                        json, "dados.lista", "Lista", colunas,
                        TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false
                )));

        assertEquals("Chave 'lista' não encontrada no JSON.", exception.getMessage());
    }

    @Test
    void gerarRelatorioPDF_DeveLancarExcecaoQuandoNaoForPossivelNavegarNoCaminho() {
        String json = "{\"dados\":[{\"nome\":\"Teste\"}]}";
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.gerarRelatorioPDF(new RelatorioRequestDTO(
                        json, "dados.lista", "Lista", colunas,
                        TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false
                )));

        assertEquals("Não foi possível navegar pelo caminho: dados.lista", exception.getMessage());
    }

    @Test
    void gerarRelatorioPDF_DeveLancarExcecaoQuandoCaminhoNaoApontarParaArray() {
        String json = "{\"dados\":{\"lista\":{\"nome\":\"Teste\"}}}";
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.gerarRelatorioPDF(new RelatorioRequestDTO(
                        json, "dados.lista", "Lista", colunas,
                        TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false
                )));

        assertEquals("O caminho 'dados.lista' não aponta para um array.", exception.getMessage());
    }

    @Test
    void gerarRelatorioPDF_DeveAceitarObjetosAninhadosComFallbackSiglaEToString() throws IOException {
        String json = """
                [
                  {
                    "nome":"Produto A",
                    "medida":{"sigla":"kg"},
                    "objetoLivre":{"codigo":1},
                    "cpf":"123",
                    "data":"2026-03-21T10:15:30-03:00",
                    "dataLocal":"2026-03-21",
                    "instante":"2026-03-21T13:15:30Z"
                  }
                ]
                """;
        Map<String, String> colunas = new LinkedHashMap<>();
        colunas.put("nome", "Nome");
        colunas.put("medida", "Medida");
        colunas.put("objetoLivre", "Objeto");
        colunas.put("cpf", "CPF");
        colunas.put("data", "Data Offset");
        colunas.put("dataLocal", "Data Local");
        colunas.put("instante", "Instante");

        byte[] pdf = service.gerarRelatorioPDF(new RelatorioRequestDTO(
                json, "", "Lista Formatada", colunas,
                TipoRelatorio.LISTA, OrientacaoRelatorio.RETRATO, false
        ));

        assertTrue(pdf.length > 500);
    }

    private byte[] gerarImagemValida() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}

