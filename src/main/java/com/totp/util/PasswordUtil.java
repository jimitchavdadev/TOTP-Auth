package com.totp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    public static String hashPassword(String password) {
        try {
            // Generate salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // Hash password with salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // Combine salt and hash
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            byte[] combined = Base64.getDecoder().decode(hashedPassword);

            // Extract salt (first 16 bytes)
            byte[] salt = new byte[16];
            System.arraycopy(combined, 0, salt, 0, 16);

            // Extract hash (remaining bytes)
            byte[] hash = new byte[combined.length - 16];
            System.arraycopy(combined, 16, hash, 0, hash.length);

            // Hash the provided password with the extracted salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] testHash = md.digest(password.getBytes());

            // Compare hashes
            return MessageDigest.isEqual(hash, testHash);

        } catch (Exception e) {
            return false;
        }
    }
}
