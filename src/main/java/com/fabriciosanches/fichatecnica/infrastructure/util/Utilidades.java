package com.fabriciosanches.fichatecnica.infrastructure.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Base64;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utilidades {
    public static Boolean validarCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }

        // Verifica se todos os dÃ­gitos sÃ£o iguais
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

        // ValidaÃ§Ã£o do CPF
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

    public static String encriptaSenha(String senha){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(senha);
    }

    // MÃ©todo para converter uma string normal em base64
    public static String encodeToBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    // MÃ©todo para converter uma string em base64 para uma string normal
    public static String decodeFromBase64(String base64Input) {
        return new String(Base64.getDecoder().decode(base64Input));
    }

    /**
     * Gera uma senha aleatÃ³ria de 10 caracteres que respeita as seguintes regras:
     * 01 - Pode conter caracteres alfanumÃ©ricos e caracteres especiais.
     * 02 - Pelo menos um caractere especial.
     * 03 - Pelo menos uma letra maiÃºscula.
     * 04 - Pelo menos uma letra minÃºscula.
     * 05 - NÃ£o pode conter espaÃ§o.
     * 06 - Pelo menos um nÃºmero.
     * 07 - Tem que comeÃ§ar por letra ou nÃºmero.
     *
     * ObservaÃ§Ãµes: O mÃ©todo sempre retorna uma senha com exatamente 10 caracteres e usa SecureRandom
     * para garantir maior entropia.
     */
    public static String gerarSenhaAleatoria() {
        final int LENGTH = 10;
        final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String LOWER = "abcdefghijklmnopqrstuvwxyz";
        final String DIGITS = "0123456789";
        final String SPECIAL = "!@#$%^&*()-_+=[]{};:,.<>?/"; // sem espaÃ§os
        final String ALPHANUM = UPPER + LOWER + DIGITS;
        final String ALL_ALLOWED = ALPHANUM + SPECIAL;

        SecureRandom random = new SecureRandom();
        char[] password = new char[LENGTH];

        // 1) Garantir que o primeiro caractere seja letra ou nÃºmero
        password[0] = ALPHANUM.charAt(random.nextInt(ALPHANUM.length()));
        boolean hasUpper = Character.isUpperCase(password[0]);
        boolean hasLower = Character.isLowerCase(password[0]);
        boolean hasDigit = Character.isDigit(password[0]);
        boolean hasSpecial = false; // nÃ£o pode ser special no primeiro

        // 2) Preparar lista de caracteres obrigatÃ³rios que faltam
        List<Character> requiredChars = new ArrayList<>();
        if (!hasUpper) requiredChars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        if (!hasLower) requiredChars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        if (!hasDigit) requiredChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        if (!hasSpecial) requiredChars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        int remainingPositions = LENGTH - 1;
        int requiredCount = requiredChars.size();

        List<Character> pool = new ArrayList<>();
        // 3) Preencher com caracteres aleatÃ³rios (exceto os que jÃ¡ garantimos) atÃ© sobrar espaÃ§o para os obrigatÃ³rios
        for (int i = 0; i < remainingPositions - requiredCount; i++) {
            pool.add(ALL_ALLOWED.charAt(random.nextInt(ALL_ALLOWED.length())));
        }
        // 4) Adicionar os caracteres obrigatÃ³rios
        pool.addAll(requiredChars);

        // 5) Embaralhar os caracteres que irÃ£o para as posiÃ§Ãµes 1..9
        Collections.shuffle(pool, random);

        for (int i = 0; i < pool.size(); i++) {
            password[i + 1] = pool.get(i);
        }

        return new String(password);
    }

}

