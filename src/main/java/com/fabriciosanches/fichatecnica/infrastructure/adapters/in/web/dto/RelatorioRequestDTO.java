package com.fabriciosanches.fichatecnica.infrastructure.adapters.in.web.dto;

import com.fabriciosanches.fichatecnica.core.domain.enums.ImagemPosicao;
import com.fabriciosanches.fichatecnica.core.domain.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.core.domain.enums.TipoRelatorio;

import java.util.Map;

/**
 * DTO de requisiÃ§Ã£o para geraÃ§Ã£o de relatÃ³rio PDF genÃ©rico.
 *
 * @param jsonData       JSON em formato String contendo os dados do relatÃ³rio.
 * @param listPath       Caminho de navegaÃ§Ã£o atÃ© a lista dentro do JSON. Deixe {@code null} ou vazio
 *                       quando o JSON raiz jÃ¡ for um array.
 * @param titulo         TÃ­tulo do relatÃ³rio exibido centralizado no cabeÃ§alho do PDF.
 * @param colunas        Mapa <b>ordenado</b> (LinkedHashMap recomendado) chaveâ†’label das colunas.
 * @param tipoRelatorio  Tipo do relatÃ³rio: LISTA ou DETALHE (default LISTA quando null).
 * @param orientacao     OrientaÃ§Ã£o do relatÃ³rio: RETRATO ou PAISAGEM (default RETRATO quando null).
 * @param alternarCores  Indica se deve alternar cores nas linhas da LISTA (default false quando null).
 * @param usarImagem     Indica se uma imagem deve ser incluÃ­da no relatÃ³rio (default false).
 *                       Quando {@code true}, {@code imagem} e {@code imagemPosicao} sÃ£o obrigatÃ³rios.
 * @param imagem         Bytes da imagem a ser inserida (PNG, JPEG etc.). ObrigatÃ³rio se {@code usarImagem = true}.
 * @param imagemPosicao  PosiÃ§Ã£o da imagem: {@link ImagemPosicao#INICIO} (antes do conteÃºdo) ou
 *                       {@link ImagemPosicao#FIM} (apÃ³s o conteÃºdo, antes do rodapÃ©).
 *                       ObrigatÃ³rio se {@code usarImagem = true}.
 * @param imagemSecundaria        Bytes de uma segunda imagem opcional (ex.: grÃ¡fico adicional).
 * @param imagemSecundariaPosicao PosiÃ§Ã£o da segunda imagem opcional.
 */
public record RelatorioRequestDTO(
        String jsonData,
        String listPath,
        String titulo,
        Map<String, String> colunas,
        TipoRelatorio tipoRelatorio,
        OrientacaoRelatorio orientacao,
        Boolean alternarCores,
        Boolean usarImagem,
        byte[] imagem,
        ImagemPosicao imagemPosicao,
        byte[] imagemSecundaria,
        ImagemPosicao imagemSecundariaPosicao
) {
    /**
     * Construtor de compatibilidade retroativa para chamadas que nÃ£o utilizam imagem.
     * Os campos {@code usarImagem}, {@code imagem} e {@code imagemPosicao} sÃ£o
     * preenchidos com {@code false}, {@code null} e {@code null} respectivamente.
     */
    public RelatorioRequestDTO(String jsonData, String listPath, String titulo,
                                Map<String, String> colunas, TipoRelatorio tipoRelatorio,
                                OrientacaoRelatorio orientacao, Boolean alternarCores) {
        this(jsonData, listPath, titulo, colunas, tipoRelatorio, orientacao,
                alternarCores, false, null, null, null, null);
    }

    /**
     * Construtor de compatibilidade para chamadas com uma Ãºnica imagem.
     */
    public RelatorioRequestDTO(String jsonData, String listPath, String titulo,
                               Map<String, String> colunas, TipoRelatorio tipoRelatorio,
                               OrientacaoRelatorio orientacao, Boolean alternarCores,
                               Boolean usarImagem, byte[] imagem, ImagemPosicao imagemPosicao) {
        this(jsonData, listPath, titulo, colunas, tipoRelatorio, orientacao,
                alternarCores, usarImagem, imagem, imagemPosicao, null, null);
    }
}

