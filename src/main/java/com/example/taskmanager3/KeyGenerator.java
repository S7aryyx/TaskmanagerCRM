package com.example.taskmanager3;

import java.util.Base64;
import java.security.SecureRandom;

public class KeyGenerator {
    public static void main(String[] args) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32]; // 256 бит (32 байта)
        secureRandom.nextBytes(key);
        String encodedKey = Base64.getEncoder().encodeToString(key);
        System.out.println(encodedKey);
    }
}
