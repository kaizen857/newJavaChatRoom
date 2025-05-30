package com.yychat.control;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtil {

    // 生成盐值
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16字节盐值
        random.nextBytes(salt);
        return salt;
    }

    // 使用SHA-256和盐值哈希密码
    public static byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes());
        return hashedPassword;
    }

    // 验证密码
    public static boolean verifyPassword(String inputPassword, byte[] storedHash, byte[] salt)
            throws NoSuchAlgorithmException {
        byte[] hashedInput = hashPassword(inputPassword, salt);
        return MessageDigest.isEqual(hashedInput, storedHash);
    }
}