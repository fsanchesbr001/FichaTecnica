package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

import com.fabriciosanches.fichatecnica.core.domain.enums.UploadJobStatus;

/**
 * DTO de resposta dos endpoints de upload de imagem de produto.
 *
 * <ul>
 *   <li>{@code jobId}     â€“ identificador Ãºnico do job assÃ­ncrono (UUID)</li>
 *   <li>{@code status}    â€“ estado atual do job: PENDING | PROCESSING | DONE | ERROR</li>
 *   <li>{@code produtoId} â€“ identificador do produto relacionado</li>
 *   <li>{@code imagemUrl} â€“ URL pÃºblica da imagem (preenchida quando status = DONE)</li>
 *   <li>{@code message}   â€“ mensagem de erro (preenchida quando status = ERROR)</li>
 * </ul>
 */
public record UploadJobDTO(
        String jobId,
        UploadJobStatus status,
        Long produtoId,
        String imagemUrl,
        String message
) {}


