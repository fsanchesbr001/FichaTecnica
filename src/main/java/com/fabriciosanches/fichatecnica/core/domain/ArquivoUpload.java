package com.fabriciosanches.fichatecnica.core.domain;

import java.util.Arrays;

public class ArquivoUpload {
    private final String nomeOriginal;
    private final String tipoConteudo;
    private final byte[] conteudo;

    public ArquivoUpload(String nomeOriginal, String tipoConteudo, byte[] conteudo) {
        this.nomeOriginal = nomeOriginal;
        this.tipoConteudo = tipoConteudo;
        this.conteudo = conteudo == null ? new byte[0] : Arrays.copyOf(conteudo, conteudo.length);
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public String getTipoConteudo() {
        return tipoConteudo;
    }

    public byte[] getConteudo() {
        return Arrays.copyOf(conteudo, conteudo.length);
    }

    public long getTamanho() {
        return conteudo.length;
    }

    public boolean isVazio() {
        return conteudo.length == 0;
    }
}

