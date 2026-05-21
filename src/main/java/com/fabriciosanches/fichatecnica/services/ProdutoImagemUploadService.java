package com.fabriciosanches.fichatecnica.services;

import com.fabriciosanches.fichatecnica.domains.Produto;
import com.fabriciosanches.fichatecnica.dtos.UploadJobDTO;
import com.fabriciosanches.fichatecnica.enums.UploadJobStatus;
import com.fabriciosanches.fichatecnica.exceptions.FichaTecnicaException;
import com.fabriciosanches.fichatecnica.repository.ProdutoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço responsável pelo upload assíncrono de imagens de produto para o
 * sistema de arquivos do Droplet DigitalOcean, servidas via Nginx.
 *
 * <p>Fluxo (Arquitetura B):</p>
 * <ol>
 *   <li>Cliente chama {@code iniciarUpload} → recebe {@code jobId} com status {@code PENDING}</li>
 *   <li>Processamento ocorre de forma assíncrona via {@code @Async}</li>
 *   <li>Cliente consulta {@code consultarStatus(jobId)} até status {@code DONE} ou {@code ERROR}</li>
 *   <li>Ao concluir, o campo {@code imagem} do {@link Produto} é atualizado com a URL pública</li>
 * </ol>
 *
 * <p>Tipos aceitos: {@code jpg, jpeg, png, webp}. Tamanho máximo configurado em
 * {@code spring.servlet.multipart.max-file-size}.</p>
 */
@Service
public class ProdutoImagemUploadService {

    private static final Logger logger = LogManager.getLogger(ProdutoImagemUploadService.class);

    /** Tipos MIME permitidos para upload de imagem. */
    private static final Set<String> MIME_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    /** Extensões permitidas (sem ponto). */
    private static final Set<String> EXT_PERMITIDAS = Set.of(
            "jpg", "jpeg", "png", "webp"
    );

    /** Tamanho máximo em bytes: 10 MB. */
    private static final long TAMANHO_MAXIMO = 10L * 1024 * 1024;

    // ─── Estado em memória dos jobs ───────────────────────────────────────────
    // Chave: jobId (UUID), Valor: estado mutável do job
    private final Map<String, JobState> jobs = new ConcurrentHashMap<>();

    // ─── Dependências ─────────────────────────────────────────────────────────
    private final ProdutoRepository produtoRepository;

    @Value("${digitalocean.storage.base-path:/olivander/ficha_tecnica/imagens}")
    private String storagePath;

    @Value("${digitalocean.storage.public-url:http://localhost:8080/uploads}")
    private String publicUrl;

    public ProdutoImagemUploadService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  API pública
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registra um novo job de upload e aciona o processamento assíncrono.
     *
     * @param produtoId identificador do produto
     * @param file      arquivo enviado pelo cliente
     * @return {@link UploadJobDTO} com status {@code PENDING}
     */
    public UploadJobDTO iniciarUpload(Long produtoId, MultipartFile file) {
        // Produto deve existir
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new FichaTecnicaException("Produto não encontrado id=" + produtoId));

        // Validações síncronas (falham antes de criar o job)
        validarArquivo(file);

        String jobId = UUID.randomUUID().toString();
        jobs.put(jobId, new JobState(jobId, UploadJobStatus.PENDING, produtoId, null, null));

        logger.info("[Upload] Job {} registrado para produto id={}", jobId, produtoId);

        // Dispara processamento assíncrono
        processarUploadAsync(jobId, produto, file);

        return toDTO(jobs.get(jobId));
    }

    /**
     * Consulta o estado atual de um job de upload.
     *
     * @param jobId identificador retornado por {@link #iniciarUpload}
     * @return {@link UploadJobDTO} com estado atual
     */
    public UploadJobDTO consultarStatus(String jobId) {
        JobState state = jobs.get(jobId);
        if (state == null) {
            throw new FichaTecnicaException("Job de upload não encontrado: " + jobId);
        }
        return toDTO(state);
    }

    /**
     * Remove a imagem associada ao produto: apaga o arquivo físico (se existir)
     * e limpa o campo {@code imagem} no banco de dados.
     *
     * @param produtoId identificador do produto
     */
    public void removerImagem(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new FichaTecnicaException("Produto não encontrado id=" + produtoId));

        String imagemAtual = produto.getImagem();
        if (imagemAtual == null || imagemAtual.isBlank()) {
            logger.info("[Upload] Produto id={} não possui imagem para remover", produtoId);
            return;
        }

        // Tenta apagar o arquivo físico (melhor esforço – não lança exceção se falhar)
        try {
            String nomeArquivo = imagemAtual.substring(imagemAtual.lastIndexOf('/') + 1);
            Path arquivo = Paths.get(storagePath, "produtos", String.valueOf(produtoId), nomeArquivo);
            Files.deleteIfExists(arquivo);
            logger.info("[Upload] Arquivo físico removido: {}", arquivo);
        } catch (IOException e) {
            logger.warn("[Upload] Falha ao remover arquivo físico do produto id={}: {}", produtoId, e.getMessage());
        }

        produto.setImagem(null);
        produtoRepository.save(produto);
        logger.info("[Upload] Imagem removida do produto id={}", produtoId);
    }

    /**
     * Retorna todos os jobs ativos (útil para monitoramento).
     *
     * @return lista de todos os jobs conhecidos
     */
    public List<UploadJobDTO> listarJobsAtivos() {
        return jobs.values().stream().map(this::toDTO).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Processamento assíncrono
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Executa o upload de forma assíncrona. Atualiza o estado do job e persiste
     * a URL no produto ao concluir com sucesso.
     */
    @Async
    protected void processarUploadAsync(String jobId, Produto produto, MultipartFile file) {
        atualizarJob(jobId, UploadJobStatus.PROCESSING, null, null);
        logger.info("[Upload] Job {} processando – produto id={}", jobId, produto.getCodigo());

        try {
            String imagemUrl = salvarArquivo(produto.getCodigo(), file);

            // Persiste a URL no produto
            produto.setImagem(imagemUrl);
            produtoRepository.save(produto);

            atualizarJob(jobId, UploadJobStatus.DONE, imagemUrl, null);
            logger.info("[Upload] Job {} concluído – URL: {}", jobId, imagemUrl);

        } catch (Exception e) {
            atualizarJob(jobId, UploadJobStatus.ERROR, null, e.getMessage());
            logger.error("[Upload] Job {} falhou – produto id={}: {}", jobId, produto.getCodigo(), e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers privados
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Salva o arquivo no diretório {@code <storagePath>/produtos/<produtoId>/}
     * com nome único gerado por UUID e retorna a URL pública Nginx-served.
     */
    private String salvarArquivo(Long produtoId, MultipartFile file) throws IOException {
        // Diretório: <base>/produtos/<produtoId>/
        Path dir = Paths.get(storagePath, "produtos", String.valueOf(produtoId))
                        .toAbsolutePath().normalize();
        Files.createDirectories(dir);

        // Nome único: <uuid>.<ext>
        String ext = obterExtensao(file.getOriginalFilename());
        String nomeArquivo = UUID.randomUUID() + "." + ext;
        Path destino = dir.resolve(nomeArquivo);

        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        // URL pública: <publicUrl>/produtos/<produtoId>/<arquivo>
        return publicUrl + "/produtos/" + produtoId + "/" + nomeArquivo;
    }

    /** Valida MIME type, extensão e tamanho do arquivo. */
    private void validarArquivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FichaTecnicaException("Arquivo de imagem não pode ser vazio.");
        }

        // Tamanho
        if (file.getSize() > TAMANHO_MAXIMO) {
            throw new FichaTecnicaException(
                    "Arquivo excede o tamanho máximo permitido de 10 MB.");
        }

        // MIME type
        String contentType = file.getContentType();
        if (contentType == null || !MIME_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new FichaTecnicaException(
                    "Tipo de arquivo não suportado: " + contentType +
                    ". Permitidos: jpg, jpeg, png, webp.");
        }

        // Extensão
        String ext = obterExtensao(file.getOriginalFilename());
        if (!EXT_PERMITIDAS.contains(ext.toLowerCase())) {
            throw new FichaTecnicaException(
                    "Extensão não permitida: " + ext +
                    ". Permitidas: jpg, jpeg, png, webp.");
        }
    }

    private String obterExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return "jpg";
        }
        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1);
    }

    private void atualizarJob(String jobId, UploadJobStatus status, String imagemUrl, String message) {
        jobs.computeIfPresent(jobId, (k, s) ->
                new JobState(k, status, s.produtoId(), imagemUrl, message));
    }

    private UploadJobDTO toDTO(JobState s) {
        return new UploadJobDTO(s.jobId(), s.status(), s.produtoId(), s.imagemUrl(), s.message());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Estado interno do job (imutável + substituição atômica via ConcurrentHashMap)
    // ─────────────────────────────────────────────────────────────────────────
    private record JobState(
            String jobId,
            UploadJobStatus status,
            Long produtoId,
            String imagemUrl,
            String message
    ) {}
}

