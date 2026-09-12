package com.fabriciosanches.fichatecnica.core.ports.in;

public interface ControleAcessoPort {
    void validarAcesso(String email);

    void errouSenha(String email);

    void resetarTentativas(String email);

    void expirarSenha(String email);
}


