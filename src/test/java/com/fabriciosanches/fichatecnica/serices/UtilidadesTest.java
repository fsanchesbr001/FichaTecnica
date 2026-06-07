package com.fabriciosanches.fichatecnica.serices;

import com.fabriciosanches.fichatecnica.util.Utilidades;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilidadesTest {

    private static final String SPECIALS = "!@#$%^&*()-_+=[]{};:,.<>?/";

    @Test
    void validarCPF_DeveRetornarTrueParaCpfValido() {
        assertTrue(Utilidades.validarCPF("52998224725"));
    }

    @Test
    void validarCPF_DeveRetornarFalseParaCasosInvalidos() {
        assertFalse(Utilidades.validarCPF(null));
        assertFalse(Utilidades.validarCPF("123"));
        assertFalse(Utilidades.validarCPF("11111111111"));
        assertFalse(Utilidades.validarCPF("52998224724"));
    }

    @Test
    void base64_DeveCodificarEDecodificarCorretamente() {
        String texto = "Senha@123";

        String codificado = Utilidades.encodeToBase64(texto);
        String decodificado = Utilidades.decodeFromBase64(codificado);

        assertNotEquals(texto, codificado);
        assertEquals(texto, decodificado);
    }

    @Test
    void encriptaSenha_DeveGerarHashValidoBCrypt() {
        String senha = "MinhaSenha@123";

        String hash = Utilidades.encriptaSenha(senha);

        assertNotNull(hash);
        assertNotEquals(senha, hash);
        assertTrue(new BCryptPasswordEncoder().matches(senha, hash));
    }

    @Test
    void gerarSenhaAleatoria_DeveRespeitarRegrasMinimas() {
        String senha = Utilidades.gerarSenhaAleatoria();

        assertNotNull(senha);
        assertEquals(10, senha.length());
        assertFalse(senha.contains(" "));
        assertTrue(Character.isLetterOrDigit(senha.charAt(0)));

        boolean hasUpper = senha.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = senha.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = senha.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = senha.chars().mapToObj(c -> (char) c).anyMatch(ch -> SPECIALS.indexOf(ch) >= 0);

        assertTrue(hasUpper);
        assertTrue(hasLower);
        assertTrue(hasDigit);
        assertTrue(hasSpecial);
    }
}

