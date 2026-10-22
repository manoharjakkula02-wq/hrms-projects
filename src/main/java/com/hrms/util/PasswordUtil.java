package com.hrms.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/** PBKDF2 password hashing. Stored format: iterations:salt:hash (Base64). */
public final class PasswordUtil {
    private static final int ITERATIONS = 65_536;
    private static final int KEY_BITS = 256;

    private PasswordUtil() {}

    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(pbkdf2(password.toCharArray(), salt, ITERATIONS));
    }

    public static boolean verify(String password, String stored) {
        try {
            String[] parts = stored.split(":");
            int iter = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            return java.security.MessageDigest.isEqual(expected, pbkdf2(password.toCharArray(), salt, iter));
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] pwd, byte[] salt, int iter) {
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(new PBEKeySpec(pwd, salt, iter, KEY_BITS)).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }
}
