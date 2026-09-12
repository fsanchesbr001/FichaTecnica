package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.domain.UnidadeMedida;
import com.fabriciosanches.fichatecnica.core.ports.in.AtualizarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.BuscarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.CriarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.core.ports.in.DeletarUnidadeMedidaPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UnidadeMedidaDTO;
import jakarta.transaction.Transactional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ficha-tecnica")
@Tag(name = "Unidades de Medida", description = "Cadastro, consulta, atualizaÃ§Ã£o, exclusÃ£o e relatÃ³rios de unidades de medida")
@SecurityRequirement(name = "bearerAuth")
public class UnidadeMedidaController {
    private static final Logger logger = LogManager.getLogger(UnidadeMedidaController.class);

    private final BuscarUnidadeMedidaPort buscarUnidadeMedidaPort;
    private final CriarUnidadeMedidaPort criarUnidadeMedidaPort;
    private final AtualizarUnidadeMedidaPort atualizarUnidadeMedidaPort;
    private final DeletarUnidadeMedidaPort deletarUnidadeMedidaPort;

    public UnidadeMedidaController(BuscarUnidadeMedidaPort buscarUnidadeMedidaPort,
                                   CriarUnidadeMedidaPort criarUnidadeMedidaPort,
                                   AtualizarUnidadeMedidaPort atualizarUnidadeMedidaPort,
                                   DeletarUnidadeMedidaPort deletarUnidadeMedidaPort) {
        this.buscarUnidadeMedidaPort = buscarUnidadeMedidaPort;
        this.criarUnidadeMedidaPort = criarUnidadeMedidaPort;
        this.atualizarUnidadeMedidaPort = atualizarUnidadeMedidaPort;
        this.deletarUnidadeMedidaPort = deletarUnidadeMedidaPort;
    }

    @GetMapping("/unidades-medida")
    @Operation(summary = "Lista unidades de medida", description = "Retorna todas as unidades de medida cadastradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<UnidadeMedidaDTO>> buscarLista() {
        logger.info("Inicio do mÃ©todo buscarLista");
        List<UnidadeMedidaDTO> medidas = buscarUnidadeMedidaPort.buscarTodos().stream()
                .map(this::toDto)
                .toList();
        logger.info("Fim do mÃ©todo buscarLista");
        return ResponseEntity.ok(medidas);
    }

    @GetMapping("/unidades-medida/{id:[0-9]+}")
    @Operation(summary = "Busca unidade de medida por ID", description = "Retorna os dados de uma unidade de medida especÃ­fica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade encontrada"),
            @ApiResponse(responseCode = "404", description = "Unidade nÃ£o encontrada")
    })
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        logger.info("Inicio do mÃ©todo buscarPorId");
        try {
            UnidadeMedida medida = buscarUnidadeMedidaPort.buscarPorId(id);
            logger.info("Fim do mÃ©todo buscarPorId");
            return ResponseEntity.ok(toDto(medida));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/unidades-medida")
    @Transactional
    @Operation(summary = "Cadastra unidade de medida", description = "Cria uma nova unidade de medida.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invÃ¡lidos para cadastro")
    })
    public ResponseEntity<?> cadastrarUnidade(@RequestBody UnidadeMedidaDTO unidade) {
        logger.info("Inicio do mÃ©todo cadastrarUnidade");
        logger.info("Cadastrando unidade de medida: {}", unidade);
        try {
            UnidadeMedida medidaCriada = criarUnidadeMedidaPort.criar(unidade.nome(), unidade.sigla());
            UnidadeMedidaDTO medida = new UnidadeMedidaDTO(
                    medidaCriada.getCodigo(),
                    medidaCriada.getNome(),
                    medidaCriada.getSigla()
            );
            logger.info("Unidade de medida cadastrada com sucesso: {}", medida);
            logger.info("Fim do mÃ©todo cadastrarUnidade");
            return ResponseEntity.ok(medida);
        }
        catch (IllegalArgumentException e){
            logger.error("Erro ao cadastrar unidade de medida", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/unidades-medida/{id:[0-9]+}")
    @Transactional
    @Operation(summary = "Atualiza unidade de medida", description = "Altera os dados de uma unidade de medida existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invÃ¡lidos"),
            @ApiResponse(responseCode = "404", description = "Unidade nÃ£o encontrada")
    })
    public ResponseEntity<?> atualizarUnidade(@PathVariable Long id, @RequestBody UnidadeMedidaDTO unidade) {
        logger.info("Inicio do mÃ©todo atualizarUnidade");
        try {
            UnidadeMedida medidaAtualizada = atualizarUnidadeMedidaPort.atualizar(id, unidade.nome(), unidade.sigla());
            logger.info("Fim do mÃ©todo atualizarUnidade");
            return ResponseEntity.ok(toDto(medidaAtualizada));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/unidades-medida/{id:[0-9]+}")
    @Transactional
    @Operation(summary = "Remove unidade de medida", description = "Exclui uma unidade de medida existente pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Unidade removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade nÃ£o encontrada"),
            @ApiResponse(responseCode = "409", description = "Unidade vinculada a outros registros")
    })
    public ResponseEntity<?> apagar(@PathVariable Long id) {
        logger.info("Inicio do mÃ©todo apagar");
        try {
            deletarUnidadeMedidaPort.deletar(id);
            logger.info("Fim do mÃ©todo apagar");
            return ResponseEntity.noContent().build();
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    private UnidadeMedidaDTO toDto(UnidadeMedida unidadeMedida) {
        return new UnidadeMedidaDTO(unidadeMedida.getCodigo(), unidadeMedida.getNome(), unidadeMedida.getSigla());
    }
}

