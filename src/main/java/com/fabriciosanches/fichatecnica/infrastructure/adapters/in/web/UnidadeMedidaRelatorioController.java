package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioDetalheUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.GerarRelatorioListaUnidadeMedidaPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ficha-tecnica/unidades-medida/relatorios")
@Tag(name = "Relatórios de Unidades de Medida", description = "Geração de relatórios PDF de lista e detalhe de unidades de medida")
@SecurityRequirement(name = "bearerAuth")
public class UnidadeMedidaRelatorioController {

    private final GerarRelatorioListaUnidadeMedidaPort gerarListaPort;
    private final GerarRelatorioDetalheUnidadeMedidaPort gerarDetalhePort;

    public UnidadeMedidaRelatorioController(GerarRelatorioListaUnidadeMedidaPort gerarListaPort,
                                            GerarRelatorioDetalheUnidadeMedidaPort gerarDetalhePort) {
        this.gerarListaPort = gerarListaPort;
        this.gerarDetalhePort = gerarDetalhePort;
    }

    @GetMapping("/lista")
    @Operation(summary = "Gera relatório de lista", description = "Gera e retorna o PDF com todas as unidades de medida.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhuma unidade encontrada para relatório"),
            @ApiResponse(responseCode = "500", description = "Erro ao gerar relatório")
    })
    public ResponseEntity<byte[]> relatorioLista() {
        try {
            byte[] relatorio = gerarListaPort.executar();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=lista_unidades_medida.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(relatorio);
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{sigla}/detalhe")
    @Operation(summary = "Gera relatório de detalhe", description = "Gera e retorna o PDF de detalhe de uma unidade de medida por sigla.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Sigla inválida"),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro ao gerar relatório")
    })
    public ResponseEntity<byte[]> relatorioDetalhe(@PathVariable String sigla) {
        try {
            byte[] relatorio = gerarDetalhePort.executar(sigla);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=detalhe_unidade_" + sigla + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(relatorio);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
