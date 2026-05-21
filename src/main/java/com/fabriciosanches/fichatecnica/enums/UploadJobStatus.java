package com.fabriciosanches.fichatecnica.enums;

/**
 * Status do job assíncrono de upload de imagem de produto.
 *
 * <ul>
 *   <li>{@code PENDING}    – job registrado, aguardando processamento</li>
 *   <li>{@code PROCESSING} – upload em andamento</li>
 *   <li>{@code DONE}       – upload concluído com sucesso; {@code imagemUrl} disponível</li>
 *   <li>{@code ERROR}      – falha no upload; detalhes em {@code message}</li>
 * </ul>
 */
public enum UploadJobStatus {
    PENDING,
    PROCESSING,
    DONE,
    ERROR
}

