package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.services.RelatorioService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller responsável pelos endpoints de geração de relatórios.
 *
 * <p>Rota base: {@code /ficha-tecnica/relatorios}</p>
 */
@RestController
@RequestMapping("ficha-tecnica/relatorios")
public class RelatorioController {

    private static final Logger logger = LogManager.getLogger(RelatorioController.class);

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    /**
     * Gera um relatório em PDF a partir de um JSON enviado no corpo da requisição
     * e retorna o arquivo pronto para download.
     *
     * <p>Exemplo de corpo da requisição:</p>
     * <pre>{@code
     * {
     *   "jsonData":  "[{\"nome\":\"Fabricio\",\"email\":\"a@b.com\",\"role\":\"ADMIN\"}]",
     *   "listPath":  "",
     *   "titulo":    "Lista de Usuários",
     *   "colunas": {
     *     "nome":  "Nome do Usuário",
     *     "email": "E-mail",
     *     "role":  "Perfil"
     *   }
     * }
     * }</pre>
     *
     * <p>O {@code listPath} pode ser vazio quando o JSON raiz já é um array.
     * Para JSONs com estrutura aninhada, use notação de ponto: {@code "dados.lista"}.</p>
     *
     * @param request {@link RelatorioRequestDTO} com os parâmetros do relatório
     * @return PDF como array de bytes com Content-Disposition para download
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SYSTEM')")
    @PostMapping("/gerar-pdf")
    public ResponseEntity<byte[]> gerarPDF(@RequestBody RelatorioRequestDTO request) {
        logger.info("Início do método gerarPDF – RelatorioController");
        logger.info("Título do relatório: '{}'", request.titulo());

        try {
            byte[] pdfBytes = relatorioService.gerarRelatorioPDF(request);

            // Gera o nome do arquivo: <Titulo-sanitizado>-YYYY-MM-DD-HH-mm-ss.pdf
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String baseFilename = request.titulo()
                    .replaceAll("[^a-zA-Z0-9À-ÿ ]", "")   // remove caracteres especiais
                    .trim()
                    .replaceAll("\\s+", "-");               // espaços → hífen
            String filename = baseFilename + "-" + timestamp + ".pdf";

            logger.info("PDF gerado com sucesso – arquivo: '{}'", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            logger.error("Parâmetros inválidos para geração do PDF: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

