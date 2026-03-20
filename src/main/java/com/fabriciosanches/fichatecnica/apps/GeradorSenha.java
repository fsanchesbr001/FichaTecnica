package com.fabriciosanches.fichatecnica.apps;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeradorSenha {
    public  String geraSenha(String senha){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(senha);
    }
    public static void main(String[] args) {
        GeradorSenha geradorSenha = new GeradorSenha();
        String senha = geradorSenha.geraSenha("@n@M@r1@S@nch35!1989");
        System.out.println(senha);
    }
}
