package com.fabriciosanches.fichatecnica.infrastructure.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class TextoEncodingUtils {

    private TextoEncodingUtils() {
    }

    public static String normalizarMojibake(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }

        if (!temIndicadorMojibake(texto)) {
            return texto;
        }

        String reparado = new String(texto.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        return contarIndicadoresMojibake(reparado) < contarIndicadoresMojibake(texto) ? reparado : texto;
    }

    public static String contentDispositionAttachment(String filename) {
        String nomeNormalizado = sanitizarNomeArquivo(filename == null ? "arquivo" : filename);
        String fallbackAscii = nomeNormalizado.replaceAll("[^\\x20-\\x7E]", "_");
        String encodedUtf8 = URLEncoder.encode(nomeNormalizado, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"" + fallbackAscii + "\"; filename*=UTF-8''" + encodedUtf8;
    }

    public static String contentDispositionInline(String filename) {
        String nomeNormalizado = sanitizarNomeArquivo(filename == null ? "arquivo" : filename);
        String fallbackAscii = nomeNormalizado.replaceAll("[^\\x20-\\x7E]", "_");
        String encodedUtf8 = URLEncoder.encode(nomeNormalizado, StandardCharsets.UTF_8).replace("+", "%20");
        return "inline; filename=\"" + fallbackAscii + "\"; filename*=UTF-8''" + encodedUtf8;
    }

    private static boolean temIndicadorMojibake(String texto) {
        return texto.indexOf('Ã') >= 0 || texto.indexOf('Â') >= 0 || texto.indexOf('â') >= 0;
    }

    private static int contarIndicadoresMojibake(String texto) {
        int total = 0;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == 'Ã' || c == 'Â' || c == 'â') {
                total++;
            }
        }
        return total;
    }

    private static String sanitizarNomeArquivo(String nomeArquivo) {
        String nomeNormalizado = normalizarMojibake(nomeArquivo);
        // Remove caracteres de controle que podem quebrar o nome do arquivo no cliente.
        String semControle = nomeNormalizado.replaceAll("[\\p{Cntrl}]", "").trim();
        return semControle.isEmpty() ? "arquivo" : semControle;
    }
}
