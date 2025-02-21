package com.fabriciosanches.contatos.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeradorSenha {
    public  String geraSenha(String senha){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(senha);
    }
    public static void main(String[] args) {
        GeradorSenha geradorSenha = new GeradorSenha();
        String senha = geradorSenha.geraSenha("F@br1c10S@nch3s");
        System.out.println(senha);
    }
}
