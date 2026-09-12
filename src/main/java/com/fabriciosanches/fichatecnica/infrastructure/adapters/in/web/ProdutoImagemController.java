package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web;

import com.fabriciosanches.fichatecnica.core.ports.in.ConsultarUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.IniciarUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarJobsUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RemoverImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.domain.ArquivoUpload;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UploadJobDTO;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("ficha-tecnica/produtos")
@Tag(name = "Imagens de Produto", description = "Upload assÃ­ncrono, monitoramento de jobs e remoÃ§Ã£o de imagens de produto")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoImagemController {

    private static final Logger logger = LogManager.getLogger(ProdutoImagemController.class);

    private final IniciarUploadImagemProdutoPort iniciarUploadImagemProdutoPort;
    private final ConsultarUploadImagemProdutoPort consultarUploadImagemProdutoPort;
    private final RemoverImagemProdutoPort removerImagemProdutoPort;
    private final ListarJobsUploadImagemProdutoPort listarJobsUploadImagemProdutoPort;

    public ProdutoImagemController(
            IniciarUploadImagemProdutoPort iniciarUploadImagemProdutoPort,
            ConsultarUploadImagemProdutoPort consultarUploadImagemProdutoPort,
            RemoverImagemProdutoPort removerImagemProdutoPort,
            ListarJobsUploadImagemProdutoPort listarJobsUploadImagemProdutoPort) {
        this.iniciarUploadImagemProdutoPort = iniciarUploadImagemProdutoPort;
        this.consultarUploadImagemProdutoPort = consultarUploadImagemProdutoPort;
        this.removerImagemProdutoPort = removerImagemProdutoPort;
        this.listarJobsUploadImagemProdutoPort = listarJobsUploadImagemProdutoPort;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/imagem/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Inicia upload de imagem", description = "Inicia o upload assÃ­ncrono da imagem de um produto e retorna o job de processamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Upload iniciado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Imagem invÃ¡lida ou dados incorretos"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao iniciar o upload")
    })
    public ResponseEntity<?> iniciarUpload(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        logger.info("[ProdutoImagemController] Iniciando upload para produto id={} | arquivo='{}' | tamanho={} bytes",
                id, file.getOriginalFilename(), file.getSize());

        try {
            ArquivoUpload arquivo = new ArquivoUpload(file.getOriginalFilename(), file.getContentType(), file.getBytes());
            UploadJobDTO job = iniciarUploadImagemProdutoPort.iniciar(id, arquivo);
            logger.info("[ProdutoImagemController] Job {} registrado para produto id={}", job.jobId(), id);
            return ResponseEntity.accepted().body(job);
        } catch (FichaTecnicaException e) {
            logger.warn("[ProdutoImagemController] Upload rejeitado para produto id={}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("[ProdutoImagemController] Erro inesperado no upload para produto id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao iniciar upload."));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/imagem/status/{jobId}")
    @Operation(summary = "Consulta status do upload", description = "Verifica o andamento de um job de upload de imagem.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Job nÃ£o encontrado"),
            @ApiResponse(responseCode = "409", description = "Job nÃ£o pertence ao produto informado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao consultar o status")
    })
    public ResponseEntity<?> consultarStatus(
            @PathVariable Long id,
            @PathVariable String jobId) {
        logger.info("[ProdutoImagemController] Consultando status do job {} para produto id={}", jobId, id);

        try {
            UploadJobDTO job = consultarUploadImagemProdutoPort.consultar(jobId);
            if (!id.equals(job.produtoId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "O job " + jobId + " nÃ£o pertence ao produto id=" + id));
            }
            return ResponseEntity.ok(job);
        } catch (FichaTecnicaException e) {
            logger.warn("[ProdutoImagemController] Job nÃ£o encontrado: {}", jobId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("[ProdutoImagemController] Erro ao consultar status do job {}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao consultar status."));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/imagem")
    @Operation(summary = "Remove imagem do produto", description = "Apaga a imagem vinculada ao produto e limpa o registro no banco.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagem removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto nÃ£o encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao remover a imagem")
    })
    public ResponseEntity<?> removerImagem(@PathVariable Long id) {
        logger.info("[ProdutoImagemController] Removendo imagem do produto id={}", id);

        try {
            removerImagemProdutoPort.remover(id);
            logger.info("[ProdutoImagemController] Imagem removida com sucesso para produto id={}", id);
            return ResponseEntity.noContent().build();
        } catch (FichaTecnicaException e) {
            logger.warn("[ProdutoImagemController] Produto nÃ£o encontrado ao remover imagem id={}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("[ProdutoImagemController] Erro inesperado ao remover imagem do produto id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno ao remover imagem."));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/imagem/jobs")
    @Operation(summary = "Lista jobs de upload", description = "Retorna todos os jobs de upload registrados na sessÃ£o atual da aplicaÃ§Ã£o.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jobs retornados com sucesso")
    })
    public ResponseEntity<List<UploadJobDTO>> listarJobs() {
        logger.info("[ProdutoImagemController] Listando todos os jobs de upload");
        return ResponseEntity.ok(listarJobsUploadImagemProdutoPort.listar());
    }
}


