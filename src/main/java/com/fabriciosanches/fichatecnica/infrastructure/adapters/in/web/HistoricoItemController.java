package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.HistoricoItem;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarHistoricoItemPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.GraficoPrecoItemDTO;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.HistoricoItemDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("ficha-tecnica")
@Tag(name = "HistÃ³rico de Itens", description = "Consulta de histÃ³rico de preÃ§os e grÃ¡fico de evoluÃ§Ã£o")
@SecurityRequirement(name = "bearerAuth")
public class HistoricoItemController {

    private static final Logger logger = LogManager.getLogger(HistoricoItemController.class);

    private final ListarHistoricoItemPort listarHistoricoItemPort;

    public HistoricoItemController(ListarHistoricoItemPort listarHistoricoItemPort) {
        this.listarHistoricoItemPort = listarHistoricoItemPort;
    }

    @GetMapping("/historico-itens")
    @Operation(summary = "Lista histÃ³rico de itens", description = "Retorna todos os registros de histÃ³rico de preÃ§os.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Erro ao buscar histÃ³rico")
    })
    public ResponseEntity<List<HistoricoItemDTO>> buscarLista() {
        try {
            List<HistoricoItemDTO> historicoItemDTOList = listarHistoricoItemPort.listar().stream().map(this::toDto).toList();
            return ResponseEntity.ok(historicoItemDTOList);
        } catch (FichaTecnicaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/historico-itens/{id}")
    @Operation(summary = "Busca histÃ³rico por ID", description = "Retorna um registro especÃ­fico do histÃ³rico de itens.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "Registro nÃ£o encontrado")
    })
    public ResponseEntity<HistoricoItemDTO> buscarPorId(@PathVariable Long id) {
        try {
            HistoricoItem historicoItem = listarHistoricoItemPort.buscarPorId(id);
            return ResponseEntity.ok(toDto(historicoItem));
        } catch (FichaTecnicaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/historico-itens/itens/{id}")
    @Operation(summary = "Busca histÃ³rico por item", description = "Retorna todos os registros de histÃ³rico de um item especÃ­fico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registros retornados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Erro ao buscar histÃ³rico do item")
    })
    public ResponseEntity<List<HistoricoItemDTO>> buscarPorItemId(@PathVariable Long id) {
        try {
            List<HistoricoItemDTO> listaHistoricoItem = listarHistoricoItemPort.listarPorCodigoItem(id).stream()
                    .map(this::toDto)
                    .toList();
            return ResponseEntity.ok(listaHistoricoItem);
        } catch (FichaTecnicaException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/historico-itens/grafico-precos/{codigoItem}")
    @Operation(summary = "Gera grÃ¡fico de preÃ§os", description = "Retorna os dados do grÃ¡fico de evoluÃ§Ã£o de preÃ§os de um item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "GrÃ¡fico gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum histÃ³rico encontrado para o item"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar o grÃ¡fico")
    })
    public ResponseEntity<GraficoPrecoItemDTO> gerarGraficoPrecos(@PathVariable Long codigoItem) {
        logger.info("InÃ­cio do mÃ©todo gerarGraficoPrecos â€“ codigoItem={}", codigoItem);
        try {
            GraficoPrecoItemDTO grafico = listarHistoricoItemPort.gerarGraficoPreco(codigoItem);
            logger.info("GrÃ¡fico de preÃ§os gerado com sucesso para codigoItem={}", codigoItem);
            return ResponseEntity.ok(grafico);
        } catch (FichaTecnicaException e) {
            logger.warn("Nenhum histÃ³rico encontrado para codigoItem={}: {}", codigoItem, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao gerar grÃ¡fico de preÃ§os para codigoItem={}", codigoItem, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private HistoricoItemDTO toDto(HistoricoItem historicoItem) {
        return new HistoricoItemDTO(
                historicoItem.getCodigo(),
                historicoItem.getCdItem(),
                historicoItem.getValor(),
                historicoItem.getDataInicio()
        );
    }
}


