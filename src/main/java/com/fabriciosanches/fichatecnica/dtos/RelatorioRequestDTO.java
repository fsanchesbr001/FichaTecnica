package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.enums.ImagemPosicao;
import com.fabriciosanches.fichatecnica.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.enums.TipoRelatorio;

import java.util.Map;

/**
 * DTO de requisição para geração de relatório PDF genérico.
 *
 * @param jsonData       JSON em formato String contendo os dados do relatório.
 * @param listPath       Caminho de navegação até a lista dentro do JSON. Deixe {@code null} ou vazio
 *                       quando o JSON raiz já for um array.
 * @param titulo         Título do relatório exibido centralizado no cabeçalho do PDF.
 * @param colunas        Mapa <b>ordenado</b> (LinkedHashMap recomendado) chave→label das colunas.
 * @param tipoRelatorio  Tipo do relatório: LISTA ou DETALHE (default LISTA quando null).
 * @param orientacao     Orientação do relatório: RETRATO ou PAISAGEM (default RETRATO quando null).
 * @param alternarCores  Indica se deve alternar cores nas linhas da LISTA (default false quando null).
 * @param usarImagem     Indica se uma imagem deve ser incluída no relatório (default false).
 *                       Quando {@code true}, {@code imagem} e {@code imagemPosicao} são obrigatórios.
 * @param imagem         Bytes da imagem a ser inserida (PNG, JPEG etc.). Obrigatório se {@code usarImagem = true}.
 * @param imagemPosicao  Posição da imagem: {@link ImagemPosicao#INICIO} (antes do conteúdo) ou
 *                       {@link ImagemPosicao#FIM} (após o conteúdo, antes do rodapé).
 *                       Obrigatório se {@code usarImagem = true}.
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
        ImagemPosicao imagemPosicao
) {
    /**
     * Construtor de compatibilidade retroativa para chamadas que não utilizam imagem.
     * Os campos {@code usarImagem}, {@code imagem} e {@code imagemPosicao} são
     * preenchidos com {@code false}, {@code null} e {@code null} respectivamente.
     */
    public RelatorioRequestDTO(String jsonData, String listPath, String titulo,
                                Map<String, String> colunas, TipoRelatorio tipoRelatorio,
                                OrientacaoRelatorio orientacao, Boolean alternarCores) {
        this(jsonData, listPath, titulo, colunas, tipoRelatorio, orientacao,
                alternarCores, false, null, null);
    }
}
