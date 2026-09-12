package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RelatorioRequestDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Controller responsÃ¡vel pelos endpoints de geraÃ§Ã£o de relatÃ³rios.
 *
 * <p>Rota base: {@code /ficha-tecnica/relatorios}</p>
 */
@RestController
@RequestMapping("ficha-tecnica/relatorios")
@Tag(name = "RelatÃ³rios", description = "GeraÃ§Ã£o de relatÃ³rios em PDF a partir de dados JSON")
@SecurityRequirement(name = "bearerAuth")
public class RelatorioController {

    private static final Logger logger = LogManager.getLogger(RelatorioController.class);

    private final GerarRelatorioPort gerarRelatorioPort;
    private final GerarGraficoPort gerarGraficoPort;

    public RelatorioController(GerarRelatorioPort gerarRelatorioPort,
                               GerarGraficoPort gerarGraficoPort) {
        this.gerarRelatorioPort = gerarRelatorioPort;
        this.gerarGraficoPort = gerarGraficoPort;
    }

    /**
     * Gera um relatÃ³rio em PDF a partir de um JSON enviado no corpo da requisiÃ§Ã£o
     * e retorna o arquivo pronto para download.
     *
     * <p>Exemplo de corpo da requisiÃ§Ã£o:</p>
     * <pre>{@code
     * {
     *   "jsonData":  "[{\"nome\":\"Fabricio\",\"email\":\"a@b.com\",\"role\":\"ADMIN\"}]",
     *   "listPath":  "",
     *   "titulo":    "Lista de UsuÃ¡rios",
     *   "colunas": {
     *     "nome":  "Nome do UsuÃ¡rio",
     *     "email": "E-mail",
     *     "role":  "Perfil"
         *   },
         *   "tipoRelatorio": "LISTA",
         *   "orientacao": "RETRATO",
         *   "alternarCores": false
     * }
     * }</pre>
     *
     * <p>O {@code listPath} pode ser vazio quando o JSON raiz jÃ¡ Ã© um array.
     * Para JSONs com estrutura aninhada, use notaÃ§Ã£o de ponto: {@code "dados.lista"}.</p>
     *
     * @param request {@link RelatorioRequestDTO} com os parÃ¢metros do relatÃ³rio
     * @return PDF como array de bytes com Content-Disposition para download
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/gerar-pdf")
    @Operation(summary = "Gera relatÃ³rio PDF", description = "Recebe os dados do relatÃ³rio em JSON e devolve o arquivo PDF pronto para download.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ParÃ¢metros invÃ¡lidos para geraÃ§Ã£o do PDF"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o PDF")
    })
    public ResponseEntity<byte[]> gerarPDF(@RequestBody RelatorioRequestDTO request) {
        logger.info("InÃ­cio do mÃ©todo gerarPDF â€“ RelatorioController");
        logger.info("TÃ­tulo do relatÃ³rio: '{}'", request.titulo());

        try {
            byte[] pdfBytes = gerarRelatorioPort.gerarRelatorioPDF(request);

            // Gera o nome do arquivo: <Titulo-sanitizado>-YYYY-MM-DD-HH-mm-ss.pdf
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String baseFilename = request.titulo()
                    .replaceAll("[^\\p{L}\\p{N} ]", "")   // remove caracteres especiais mantendo letras/numeros Unicode
                    .trim()
                    .replaceAll("\\s+", "-");               // espaÃ§os â†’ hÃ­fen
            String filename = baseFilename + "-" + timestamp + ".pdf";

            logger.info("PDF gerado com sucesso â€“ arquivo: '{}'", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            logger.error("ParÃ¢metros invÃ¡lidos para geraÃ§Ã£o do PDF: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}



