package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.ports.in.GerarGraficoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.RelatorioRequestDTO;
import com.fabriciosanches.fichatecnica.infrastructure.util.TextoEncodingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("ficha-tecnica/relatorios")
@Tag(name = "Relatorios", description = "Geracao de relatorios em PDF a partir de dados JSON")
@SecurityRequirement(name = "bearerAuth")
public class RelatorioController {

    private static final Logger logger = LogManager.getLogger(RelatorioController.class);

    private final GerarRelatorioPort gerarRelatorioPort;

    public RelatorioController(GerarRelatorioPort gerarRelatorioPort,
                               GerarGraficoPort gerarGraficoPort) {
        this.gerarRelatorioPort = gerarRelatorioPort;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/gerar-pdf")
    @Operation(summary = "Gera relatorio PDF", description = "Recebe os dados do relatorio em JSON e devolve o arquivo PDF pronto para download.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos para geracao do PDF"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o PDF")
    })
    public ResponseEntity<byte[]> gerarPDF(@RequestBody RelatorioRequestDTO request) {
        logger.info("Inicio do metodo gerarPDF - RelatorioController");
        logger.info("Titulo do relatorio: '{}'", request.titulo());

        try {
            byte[] pdfBytes = gerarRelatorioPort.gerarRelatorioPDF(request);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String baseFilename = TextoEncodingUtils.normalizarMojibake(request.titulo())
                    .replaceAll("[^\\p{L}\\p{N} ]", "")
                    .trim()
                    .replaceAll("\\s+", "-");
            String filename = baseFilename + "-" + timestamp + ".pdf";

            logger.info("PDF gerado com sucesso - arquivo: '{}'", filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, TextoEncodingUtils.contentDispositionAttachment(filename))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            logger.error("Parametros invalidos para geracao do PDF: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
