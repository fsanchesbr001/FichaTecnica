package com.fabriciosanches.fichatecnica.dtos;

import com.fabriciosanches.fichatecnica.enums.OrientacaoRelatorio;
import com.fabriciosanches.fichatecnica.enums.TipoRelatorio;

import java.util.Map;

/**
 * DTO de requisição para geração de relatório PDF genérico.
 *
 * @param jsonData  JSON em formato String contendo os dados do relatório.
 *                  Pode ser um array JSON ou um objeto contendo um array
 *                  acessível via {@code listPath}.
 * @param listPath  Caminho de navegação até a lista dentro do JSON
 *                  (ex.: "usuarios.lista"). Deixe {@code null} ou vazio
 *                  quando o JSON raiz já for um array.
 * @param titulo    Título do relatório exibido centralizado no cabeçalho do PDF.
 * @param colunas   Mapa <b>ordenado</b> (LinkedHashMap recomendado) que relaciona
 *                  o nome do atributo no JSON ao nome da coluna exibida no relatório.
 *                  Exemplo: {@code { "nome": "Nome do Usuário", "email": "E-mail", "role": "Perfil" }}.
 * @param tipoRelatorio Tipo do relatório: LISTA ou DETALHE (default LISTA quando null).
 * @param orientacao Orientacao do relatório: RETRATO ou PAISAGEM (default RETRATO quando null).
 * @param alternarCores Indica se deve alternar cores nas linhas da LISTA (default false quando null).
 */
public record RelatorioRequestDTO(
        String jsonData,
        String listPath,
        String titulo,
        Map<String, String> colunas,
        TipoRelatorio tipoRelatorio,
        OrientacaoRelatorio orientacao,
        Boolean alternarCores
) {
}

