package com.fabriciosanches.fichatecnica.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Base64;

public class Utilidades {
    public static Boolean validarCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais
        boolean allEqual = true;
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                allEqual = false;
                break;
            }
        }
        if (allEqual) {
            return false;
        }

        // Validação do CPF
        int soma = 0;
        int peso = 10;

        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * peso--;
        }

        int primeiroDigitoVerificador = 11 - (soma % 11);
        if (primeiroDigitoVerificador >= 10) {
            primeiroDigitoVerificador = 0;
        }

        soma = 0;
        peso = 11;

        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * peso--;
        }

        int segundoDigitoVerificador = 11 - (soma % 11);
        if (segundoDigitoVerificador >= 10) {
            segundoDigitoVerificador = 0;
        }

        return primeiroDigitoVerificador == Character.getNumericValue(cpf.charAt(9))
                && segundoDigitoVerificador == Character.getNumericValue(cpf.charAt(10));
    }

    public static String geraSenha(String senha){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(senha);
    }

    // Método para converter uma string normal em base64
    public static String encodeToBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    // Método para converter uma string em base64 para uma string normal
    public static String decodeFromBase64(String base64Input) {
        return new String(Base64.getDecoder().decode(base64Input));
    }

}
