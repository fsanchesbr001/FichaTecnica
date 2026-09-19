package com.fabriciosanches.fichatecnica.core.usecase;

import com.fabriciosanches.fichatecnica.core.domain.Produto;
import com.fabriciosanches.fichatecnica.core.domain.ArquivoUpload;
import com.fabriciosanches.fichatecnica.core.ports.in.ConsultarUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.IniciarUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.ListarJobsUploadImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.in.RemoverImagemProdutoPort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoImagemStoragePort;
import com.fabriciosanches.fichatecnica.core.ports.out.ProdutoRepositoryPort;
import com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto.UploadJobDTO;
import com.fabriciosanches.fichatecnica.core.domain.enums.UploadJobStatus;
import com.fabriciosanches.fichatecnica.core.exceptions.FichaTecnicaException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProdutoImagemUploadUseCase implements IniciarUploadImagemProdutoPort, ConsultarUploadImagemProdutoPort,
        RemoverImagemProdutoPort, ListarJobsUploadImagemProdutoPort {

    private final ProdutoRepositoryPort produtoRepositoryPort;
    private final ProdutoImagemStoragePort produtoImagemStoragePort;
    private final Map<String, JobState> jobs = new ConcurrentHashMap<>();

    public ProdutoImagemUploadUseCase(ProdutoRepositoryPort produtoRepositoryPort, ProdutoImagemStoragePort produtoImagemStoragePort) {
        this.produtoRepositoryPort = Objects.requireNonNull(produtoRepositoryPort, "ProdutoRepositoryPort não pode ser nulo");
        this.produtoImagemStoragePort = Objects.requireNonNull(produtoImagemStoragePort, "ProdutoImagemStoragePort não pode ser nulo");
    }

    @Override
    public UploadJobDTO iniciar(Long produtoId, ArquivoUpload arquivo) {
        Produto produto = produtoRepositoryPort.buscarPorId(produtoId)
                .orElseThrow(() -> new FichaTecnicaException("Produto não encontrado id=" + produtoId));

        validarArquivo(arquivo);
        String jobId = UUID.randomUUID().toString();
        jobs.put(jobId, new JobState(jobId, UploadJobStatus.PENDING, produtoId, null, null));
        atualizarJob(jobId, UploadJobStatus.PROCESSING, null, null);

        try {
            String imagemUrl = produtoImagemStoragePort.salvar(produtoId, arquivo.getNomeOriginal(), arquivo.getTipoConteudo(), arquivo.getConteudo());
            produto.setImagem(imagemUrl);
            produtoRepositoryPort.salvar(produto);
            atualizarJob(jobId, UploadJobStatus.DONE, imagemUrl, null);
        } catch (Exception e) {
            atualizarJob(jobId, UploadJobStatus.ERROR, null, e.getMessage());
        }

        return toDTO(jobs.get(jobId));
    }

    @Override
    public UploadJobDTO consultar(String jobId) {
        JobState state = jobs.get(jobId);
        if (state == null) {
            throw new FichaTecnicaException("Job de upload nao encontrado: " + jobId);
        }
        return toDTO(state);
    }

    @Override
    public void remover(Long produtoId) {
        Produto produto = produtoRepositoryPort.buscarPorId(produtoId)
                .orElseThrow(() -> new FichaTecnicaException("Produto nao encontrado id=" + produtoId));

        String imagemAtual = produto.getImagem();
        if (imagemAtual == null || imagemAtual.isBlank()) {
            return;
        }

        produtoImagemStoragePort.remover(imagemAtual, produtoId);
        produto.setImagem(null);
        produtoRepositoryPort.salvar(produto);
    }

    @Override
    public List<UploadJobDTO> listar() {
        return jobs.values().stream().map(this::toDTO).toList();
    }

    private void validarArquivo(ArquivoUpload arquivo) {
        if (arquivo == null || arquivo.isVazio()) {
            throw new FichaTecnicaException("Arquivo de imagem nao pode ser vazio.");
        }
        if (arquivo.getTamanho() > 10L * 1024 * 1024) {
            throw new FichaTecnicaException("Arquivo excede o tamanho maximo permitido de 10 MB.");
        }
        String contentType = arquivo.getTipoConteudo();
        if (contentType == null || !(contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/png") || contentType.equalsIgnoreCase("image/webp"))) {
            throw new FichaTecnicaException("Tipo de arquivo nao suportado: " + contentType + ". Permitidos: jpg, jpeg, png, webp.");
        }
        String original = arquivo.getNomeOriginal();
        String ext = original == null || !original.contains(".") ? "jpg" : original.substring(original.lastIndexOf('.') + 1);
        if (!(ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("webp"))) {
            throw new FichaTecnicaException("Extensao nao permitida: " + ext + ". Permitidas: jpg, jpeg, png, webp.");
        }
    }

    private void atualizarJob(String jobId, UploadJobStatus status, String imagemUrl, String message) {
        jobs.computeIfPresent(jobId, (k, s) -> new JobState(k, status, s.produtoId(), imagemUrl, message));
    }

    private UploadJobDTO toDTO(JobState state) {
        return new UploadJobDTO(state.jobId(), state.status(), state.produtoId(), state.imagemUrl(), state.message());
    }

    private record JobState(String jobId, UploadJobStatus status, Long produtoId, String imagemUrl, String message) {}
}


