package com.fabriciosanches.fichatecnica.enums;

/**
 * Define a posição da imagem opcional em um relatório PDF.
 *
 * <ul>
 *   <li>{@link #INICIO} – a imagem é exibida antes do conteúdo do relatório,
 *       imediatamente após o cabeçalho da primeira página.</li>
 *   <li>{@link #FIM}    – a imagem é exibida após todo o conteúdo do relatório
 *       e antes do rodapé.</li>
 * </ul>
 *
 * Só é relevante quando {@code usarImagem = true} no {@code RelatorioRequestDTO}.
 */
public enum ImagemPosicao {
    INICIO,
    FIM
}

