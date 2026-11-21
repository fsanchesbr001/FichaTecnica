package com.fabriciosanches.fichatecnica.apps;

public class OutboxEncriptKey {
    public static void main(String[] args) {
        //Gerar String Aleatória de 32 Bytes
        String randomString = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        String encodedKey = java.util.Base64.getEncoder().encodeToString(randomString.getBytes());
        System.out.println("Encoded Key: " + encodedKey);
    }
}
