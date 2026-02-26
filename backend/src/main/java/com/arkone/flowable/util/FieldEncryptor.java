package com.arkone.flowable.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 字段加密器 - 基于AES/GCM实现
 *
 * <p>提供敏感字段的加密和解密功能，使用AES/GCM/NoPadding算法。
 * GCM模式提供认证加密（AEAD），确保数据的机密性和完整性。</p>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li>算法：AES/GCM/NoPadding</li>
 *   <li>密钥长度：256位（通过SHA-256派生）</li>
 *   <li>IV长度：12字节（随机生成）</li>
 *   <li>认证标签：128位</li>
 *   <li>每次加密使用不同的随机IV</li>
 * </ul>
 *
 * <h3>数据格式：</h3>
 * <pre>
 * 加密后的Base64字符串 = Base64(IV[12字节] + 密文 + 认证标签[16字节])
 * </pre>
 *
 * <h3>配置示例：</h3>
 * <pre>
 * # application.yml
 * encryption:
 *   key: ${ENCRYPTION_KEY:your-secret-key-change-in-production}
 * </pre>
 *
 * @author ArkOne Team
 * @version 1.0
 * @since 2026-02-25
 */
@Component
public class FieldEncryptor {

    private static final Logger logger = LoggerFactory.getLogger(FieldEncryptor.class);

    /**
     * 加密算法：AES/GCM/NoPadding
     */
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    /**
     * GCM认证标签长度（位）
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * GCM初始化向量长度（字节）
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * 加密密钥（从配置文件注入）
     */
    @Value("${encryption.key:default-key-please-change-in-production}")
    private String encryptionKey;

    /**
     * 安全随机数生成器
     */
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * 获取密钥
     *
     * <p>使用SHA-256对配置的密钥进行哈希，确保密钥长度为256位。</p>
     *
     * @return AES密钥
     * @throws EncryptionException 当密钥生成失败时抛出
     */
    private SecretKey getSecretKey() throws EncryptionException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to generate secret key", e);
            throw new EncryptionException("密钥生成失败", e);
        }
    }

    /**
     * 加密明文
     *
     * <p>使用AES/GCM算法加密数据。每次加密都会生成新的随机IV，
     * 确保相同的明文每次加密后的密文都不同。</p>
     *
     * @param plainText 明文字符串
     * @return Base64编码的加密字符串（包含IV和密文）
     * @throws EncryptionException 当加密失败时抛出
     */
    public String encrypt(String plainText) throws EncryptionException {
        if (plainText == null || plainText.isEmpty()) {
            throw new EncryptionException("明文不能为空");
        }

        try {
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // 初始化加密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), parameterSpec);

            // 加密数据
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 将IV和密文组合: IV(12字节) + 密文(包含16字节认证标签)
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            String result = Base64.getEncoder().encodeToString(combined);
            logger.debug("Encrypted data successfully, length: {}", result.length());
            return result;

        } catch (Exception e) {
            logger.error("Encryption failed for input length: {}", plainText.length(), e);
            throw new EncryptionException("加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密密文
     *
     * <p>从加密字符串中提取IV和密文，然后使用AES/GCM算法解密。
     * GCM模式会自动验证认证标签，确保数据未被篡改。</p>
     *
     * @param encryptedText Base64编码的加密字符串
     * @return 解密后的明文字符串
     * @throws EncryptionException 当解密失败或数据被篡改时抛出
     */
    public String decrypt(String encryptedText) throws EncryptionException {
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new EncryptionException("密文不能为空");
        }

        try {
            // 解码Base64
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // 验证数据长度
            if (combined.length < GCM_IV_LENGTH) {
                throw new EncryptionException("密文格式无效：长度不足");
            }

            // 分离IV和密文
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            // 初始化解密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), parameterSpec);

            // 解密数据（GCM会自动验证认证标签）
            byte[] decrypted = cipher.doFinal(encrypted);
            String result = new String(decrypted, StandardCharsets.UTF_8);
            logger.debug("Decrypted data successfully, length: {}", result.length());
            return result;

        } catch (javax.crypto.AEADBadTagException e) {
            logger.error("Decryption failed: authentication tag mismatch", e);
            throw new EncryptionException("解密失败：数据已被篡改或密钥错误", e);
        } catch (Exception e) {
            logger.error("Decryption failed", e);
            throw new EncryptionException("解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量加密
     *
     * @param plainTexts 明文数组
     * @return 加密后的字符串数组
     * @throws EncryptionException 当任何一个加密失败时抛出
     */
    public String[] encryptBatch(String[] plainTexts) throws EncryptionException {
        if (plainTexts == null) {
            throw new EncryptionException("输入数组不能为空");
        }

        String[] results = new String[plainTexts.length];
        for (int i = 0; i < plainTexts.length; i++) {
            results[i] = encrypt(plainTexts[i]);
        }
        return results;
    }

    /**
     * 批量解密
     *
     * @param encryptedTexts 密文数组
     * @return 解密后的字符串数组
     * @throws EncryptionException 当任何一个解密失败时抛出
     */
    public String[] decryptBatch(String[] encryptedTexts) throws EncryptionException {
        if (encryptedTexts == null) {
            throw new EncryptionException("输入数组不能为空");
        }

        String[] results = new String[encryptedTexts.length];
        for (int i = 0; i < encryptedTexts.length; i++) {
            results[i] = decrypt(encryptedTexts[i]);
        }
        return results;
    }

    /**
     * 加密异常
     */
    public static class EncryptionException extends Exception {
        public EncryptionException(String message) {
            super(message);
        }

        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
