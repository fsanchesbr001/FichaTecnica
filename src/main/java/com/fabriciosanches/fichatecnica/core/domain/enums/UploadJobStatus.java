package com.fabriciosanches.fichatecnica.core.domain.enums;

/**
 * Status do job assÃ­ncrono de upload de imagem de produto.
 *
 * <ul>
 *   <li>{@code PENDING}    â€“ job registrado, aguardando processamento</li>
 *   <li>{@code PROCESSING} â€“ upload em andamento</li>
 *   <li>{@code DONE}       â€“ upload concluÃ­do com sucesso; {@code imagemUrl} disponÃ­vel</li>
 *   <li>{@code ERROR}      â€“ falha no upload; detalhes em {@code message}</li>
 * </ul>
 */
public enum UploadJobStatus {
    PENDING,
    PROCESSING,
    DONE,
    ERROR
}


