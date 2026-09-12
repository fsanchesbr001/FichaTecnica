package com.fabriciosanches.fichatecnica.core.domain.enums;

/**
 * Define a posiÃ§Ã£o da imagem opcional em um relatÃ³rio PDF.
 *
 * <ul>
 *   <li>{@link #INICIO} â€“ a imagem Ã© exibida antes do conteÃºdo do relatÃ³rio,
 *       imediatamente apÃ³s o cabeÃ§alho da primeira pÃ¡gina.</li>
 *   <li>{@link #FIM}    â€“ a imagem Ã© exibida apÃ³s todo o conteÃºdo do relatÃ³rio
 *       e antes do rodapÃ©.</li>
 * </ul>
 *
 * SÃ³ Ã© relevante quando {@code usarImagem = true} no {@code RelatorioRequestDTO}.
 */
public enum ImagemPosicao {
    INICIO,
    FIM
}


