package com.fabriciosanches.fichatecnica.apps;

import com.fabriciosanches.fichatecnica.util.Utilidades;

public class TesteBase64 {
    public static void main(String[] args) {
        String base64 = "eyJ1c2VyaWQiOiIxIiwibG9naW4iOiJmYWNpb3NAY29udGV4dG8uY29tIiwicGFzc3dvcmQiOiIkMmEkM2QkN2QkZDYwYjE0YjA5YjQzZDUxZWIyMjg0YjEwM2U0NmQzYjIifQ==";
        String decoded = Utilidades.decodeFromBase64(base64);
        System.out.println(decoded);

        // Exemplo de uso do método encodeToBase64
        String input = "Santander!037";
        String encoded = Utilidades.encodeToBase64(input);
        System.out.println("Encoded: " + encoded);
    }
}
