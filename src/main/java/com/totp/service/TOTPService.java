// ============================================================================
// TOTP Service (Authentication Logic)
// ============================================================================
package com.totp.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class TOTPService {
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final String APP_NAME = "TOTP-Auth";

    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20]; // 160 bits
        random.nextBytes(bytes);
        return base32Encode(bytes);
    }

    public String generateQRCodeURL(String email, String secret) {
        try {
            String issuer = URLEncoder.encode(APP_NAME, StandardCharsets.UTF_8.toString());
            String account = URLEncoder.encode(email, StandardCharsets.UTF_8.toString());

            return String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                    issuer, account, secret, issuer
            );
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR code URL", e);
        }
    }

    public boolean verifyCode(String secret, String userCode) {
        String expectedCode = generateTOTP(secret);
        String previousCode = generateTOTP(secret, System.currentTimeMillis() - 30000);

        return userCode.equals(expectedCode) || userCode.equals(previousCode);
    }

    private String generateTOTP(String secret) {
        return generateTOTP(secret, System.currentTimeMillis());
    }

    private String generateTOTP(String secret, long timeMillis) {
        try {
            long timeStep = timeMillis / 30000; // 30-second intervals
            byte[] timeBytes = longToBytes(timeStep);
            byte[] secretBytes = base32Decode(secret);

            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(secretBytes, "HmacSHA1");
            mac.init(keySpec);

            byte[] hash = mac.doFinal(timeBytes);
            int offset = hash[hash.length - 1] & 0x0F;

            int binary = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);

            int otp = binary % 1000000;
            return String.format("%06d", otp);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating TOTP", e);
        }
    }

    private String base32Encode(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;

        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                result.append(BASE32_CHARS.charAt((buffer >> (bitsLeft - 5)) & 31));
                bitsLeft -= 5;
            }
        }

        if (bitsLeft > 0) {
            result.append(BASE32_CHARS.charAt((buffer << (5 - bitsLeft)) & 31));
        }

        return result.toString();
    }

    private byte[] base32Decode(String encoded) {
        encoded = encoded.toUpperCase().replaceAll("[^A-Z2-7]", "");

        if (encoded.length() == 0) return new byte[0];

        int encodedLength = encoded.length();
        int outLength = encodedLength * 5 / 8;
        byte[] result = new byte[outLength];

        int buffer = 0;
        int bitsLeft = 0;
        int count = 0;

        for (char c : encoded.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;

            buffer = (buffer << 5) | val;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                result[count++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }

        return Arrays.copyOf(result, count);
    }

    private byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }
}

