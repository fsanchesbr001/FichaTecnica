package com.fabriciosanches.fichatecnica.controllers;

import com.fabriciosanches.fichatecnica.dtos.UploadJobDTO;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.services.ProdutoImagemUploadService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * Controller responsável pelo upload assíncrono de imagens de Produto
 * para o sistema de arquivos do Droplet DigitalOcean (servido via Nginx).
 *
 * <p>Fluxo (Arquitetura B):</p>
 * <ol>
 *   <li>{@code POST   /ficha-tecnica/produtos/{id}/imagem/upload}   → inicia o job, retorna 202 + jobId</li>
 *   <li>{@code GET    /ficha-tecnica/produtos/{id}/imagem/status/{jobId}} → consulta o andamento</li>
 *   <li>{@code DELETE /ficha-tecnica/produtos/{id}/imagem}           → remove o vínculo da imagem</li>
 *   <li>{@code GET    /ficha-tecnica/produtos/imagem/jobs}           → lista todos os jobs (ADMIN)</li>
 * </ol>
 */
@RestController
@RequestMapping("ficha-tecnica/produtos")
@Tag(name = "Imagens de Produto", description = "Upload assíncrono, monitoramento de jobs e remoção de imagens de produto")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoImagemController {

    private static final Logger logger = LogManager.getLogger(ProdutoImagemController.class);

    private final ProdutoImagemUploadService uploadService;

    public ProdutoImagemController(ProdutoImagemUploadService uploadService) {
        this.uploadService = uploadService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  POST /ficha-tecnica/produtos/{id}/imagem/upload
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Inicia o upload assíncrono da imagem do produto.
     *
     * <p>Aceita {@code multipart/form-data} com o campo {@code file}.</p>
     * <p>Tipos permitidos: {@code jpg, jpeg, png, webp} — máximo 10 MB.</p>
     *
     * @param id   código do produto
     * @param file arquivo de imagem enviado pelo cliente
     * @return 202 Accepted com {@link UploadJobDTO} contendo o {@code jobId}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/imagem/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Inicia upload de imagem", description = "Inicia o upload assíncrono da imagem de um produto e retorna o job de processamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Upload iniciado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Imagem inválida ou dados incorretos"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao iniciar o upload")
    })
    public ResponseEntity<?> iniciarUpload(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {

        logger.info("[ProdutoImagemController] Iniciando upload para produto id={} | arquivo='{}' | tamanho={} bytes",
                id, file.getOriginalFilename(), file.getSize());

        try {
            UploadJobDTO job = uploadService.iniciarUpload(id, file);
            logger.info("[ProdutoImagemController] Job {} registrado para produto id={}", job.jobId(), id);
            return ResponseEntity.accepted().body(job);  // 202 Accepted

        } catch (FichaTecnicaException e) {
            logger.warn("[ProdutoImagemController] Upload rejeitado para produto id={}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("[ProdutoImagemController] Erro inesperado no upload para produto id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao iniciar upload."));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GET /ficha-tecnica/produtos/{id}/imagem/status/{jobId}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Consulta o estado atual de um job de upload.
     *
     * <p>O cliente deve fazer polling neste endpoint até receber
     * {@code status = DONE} (ou {@code ERROR}).</p>
     *
     * @param id    código do produto
     * @param jobId identificador do job retornado por {@code /upload}
     * @return 200 OK com {@link UploadJobDTO} atualizado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/imagem/status/{jobId}")
    @Operation(summary = "Consulta status do upload", description = "Verifica o andamento de um job de upload de imagem.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Job não encontrado"),
            @ApiResponse(responseCode = "409", description = "Job não pertence ao produto informado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao consultar o status")
    })
    public ResponseEntity<?> consultarStatus(
            @PathVariable Long id,
            @PathVariable String jobId) {

        logger.info("[ProdutoImagemController] Consultando status do job {} para produto id={}", jobId, id);

        try {
            UploadJobDTO job = uploadService.consultarStatus(jobId);

            // Garante que o job pertence ao produto informado na URL
            if (!id.equals(job.produtoId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "O job " + jobId + " não pertence ao produto id=" + id));
            }

            return ResponseEntity.ok(job);

        } catch (FichaTecnicaException e) {
            logger.warn("[ProdutoImagemController] Job não encontrado: {}", jobId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("[ProdutoImagemController] Erro ao consultar status do job {}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao consultar status."));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DELETE /ficha-tecnica/produtos/{id}/imagem
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Remove a imagem do produto: apaga o arquivo físico no Droplet e limpa
     * o campo {@code imagem} no banco de dados.
     *
     * @param id código do produto
     * @return 204 No Content em caso de sucesso
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/imagem")
    @Operation(summary = "Remove imagem do produto", description = "Apaga a imagem vinculada ao produto e limpa o registro no banco.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagem removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao remover a imagem")
    })
    public ResponseEntity<?> removerImagem(@PathVariable Long id) {
        logger.info("[ProdutoImagemController] Removendo imagem do produto id={}", id);

        try {
            uploadService.removerImagem(id);
            logger.info("[ProdutoImagemController] Imagem removida com sucesso para produto id={}", id);
            return ResponseEntity.noContent().build();  // 204

        } catch (FichaTecnicaException e) {
            logger.warn("[ProdutoImagemController] Produto não encontrado ao remover imagem id={}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("[ProdutoImagemController] Erro inesperado ao remover imagem do produto id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao remover imagem."));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GET /ficha-tecnica/produtos/imagem/jobs
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Lista todos os jobs de upload registrados na sessão atual da aplicação.
     * Útil para monitoramento e debug.
     *
     * @return lista de {@link UploadJobDTO} com todos os jobs
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/imagem/jobs")
    @Operation(summary = "Lista jobs de upload", description = "Retorna todos os jobs de upload registrados na sessão atual da aplicação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jobs retornados com sucesso")
    })
    public ResponseEntity<List<UploadJobDTO>> listarJobs() {
        logger.info("[ProdutoImagemController] Listando todos os jobs de upload");
        return ResponseEntity.ok(uploadService.listarJobsAtivos());
    }
}

