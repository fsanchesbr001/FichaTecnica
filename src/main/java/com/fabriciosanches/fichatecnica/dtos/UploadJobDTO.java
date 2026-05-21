package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.enums.UploadJobStatus;

/**
 * DTO de resposta dos endpoints de upload de imagem de produto.
 *
 * <ul>
 *   <li>{@code jobId}     – identificador único do job assíncrono (UUID)</li>
 *   <li>{@code status}    – estado atual do job: PENDING | PROCESSING | DONE | ERROR</li>
 *   <li>{@code produtoId} – identificador do produto relacionado</li>
 *   <li>{@code imagemUrl} – URL pública da imagem (preenchida quando status = DONE)</li>
 *   <li>{@code message}   – mensagem de erro (preenchida quando status = ERROR)</li>
 * </ul>
 */
public record UploadJobDTO(
        String jobId,
        UploadJobStatus status,
        Long produtoId,
        String imagemUrl,
        String message
) {}

