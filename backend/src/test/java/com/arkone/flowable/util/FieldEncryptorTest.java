package com.arkone.flowable.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldEncryptor单元测试
 *
 * @author ArkOne Team
 * @version 1.0
 * @since 2026-02-25
 */
@DisplayName("字段加密器测试")
class FieldEncryptorTest {

    private FieldEncryptor fieldEncryptor;
    private static final String TEST_KEY = "test-encryption-key-for-unit-testing";

    @BeforeEach
    void setUp() {
        fieldEncryptor = new FieldEncryptor();
        // 使用反射设置测试密钥
        ReflectionTestUtils.setField(fieldEncryptor, "encryptionKey", TEST_KEY);
    }

    @Test
    @DisplayName("测试基本加密解密")
    void testBasicEncryptDecrypt() throws FieldEncryptor.EncryptionException {
        String plainText = "Hello, World!";

        String encrypted = fieldEncryptor.encrypt(plainText);
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);

        String decrypted = fieldEncryptor.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("测试中文字符加密解密")
    void testChineseCharacters() throws FieldEncryptor.EncryptionException {
        String plainText = "测序流程配置系统";

        String encrypted = fieldEncryptor.encrypt(plainText);
        assertNotNull(encrypted);

        String decrypted = fieldEncryptor.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("测试特殊字符加密解密")
    void testSpecialCharacters() throws FieldEncryptor.EncryptionException {
        String plainText = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";

        String encrypted = fieldEncryptor.encrypt(plainText);
        String decrypted = fieldEncryptor.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("测试长文本加密解密")
    void testLongText() throws FieldEncryptor.EncryptionException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("这是一段很长的测试文本，用于验证加密器对长文本的处理能力。");
        }
        String plainText = sb.toString();

        String encrypted = fieldEncryptor.encrypt(plainText);
        String decrypted = fieldEncryptor.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("测试空字符串加密")
    void testEmptyString() {
        FieldEncryptor.EncryptionException exception = assertThrows(
            FieldEncryptor.EncryptionException.class,
            () -> fieldEncryptor.encrypt("")
        );
        assertTrue(exception.getMessage().contains("明文不能为空"));
    }

    @Test
    @DisplayName("测试null加密")
    void testNullEncryption() {
        FieldEncryptor.EncryptionException exception = assertThrows(
            FieldEncryptor.EncryptionException.class,
            () -> fieldEncryptor.encrypt(null)
        );
        assertTrue(exception.getMessage().contains("明文不能为空"));
    }

    @Test
    @DisplayName("测试null解密")
    void testNullDecryption() {
        FieldEncryptor.EncryptionException exception = assertThrows(
            FieldEncryptor.EncryptionException.class,
            () -> fieldEncryptor.decrypt(null)
        );
        assertTrue(exception.getMessage().contains("密文不能为空"));
    }

    @Test
    @DisplayName("测试空字符串解密")
    void testEmptyStringDecryption() {
        FieldEncryptor.EncryptionException exception = assertThrows(
            FieldEncryptor.EncryptionException.class,
            () -> fieldEncryptor.decrypt("")
        );
        assertTrue(exception.getMessage().contains("密文不能为空"));
    }

    @Test
    @DisplayName("测试无效Base64解密")
    void testInvalidBase64() {
        FieldEncryptor.EncryptionException exception = assertThrows(
            FieldEncryptor.EncryptionException.class,
            () -> fieldEncryptor.decrypt("invalid-base64-string!!!")
        );
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("测试密文长度不足")
    void testInsufficientCiphertextLength() {
        // 创建一个长度不足的Base64字符串（少于12字节IV）
        String shortCiphertext = Base64.getEncoder().encodeToString(new byte[5]);

        FieldEncryptor.EncryptionException exception = assertThrows(
            FieldEncryptor.EncryptionException.class,
            () -> fieldEncryptor.decrypt(shortCiphertext)
        );
        assertTrue(exception.getMessage().contains("密文格式无效"));
    }

    @Test
    @DisplayName("测试篡改密文检测")
    void testTamperedCiphertext() throws FieldEncryptor.EncryptionException {
        String plainText = "Sensitive Data";
        String encrypted = fieldEncryptor.encrypt(plainText);

        // 篡改密文
        byte[] cipherBytes = Base64.getDecoder().decode(encrypted);
        cipherBytes[cipherBytes.length - 1] ^= 0xFF; // 翻转最后一个字节
        String tamperedCiphertext = Base64.getEncoder().encodeToString(cipherBytes);

        // GCM模式应该检测到篡改
        FieldEncryptor.EncryptionException exception = assertThrows(
            FieldEncryptor.EncryptionException.class,
            () -> fieldEncryptor.decrypt(tamperedCiphertext)
        );
        assertTrue(exception.getMessage().contains("数据已被篡改") ||
                   exception.getMessage().contains("解密失败"));
    }

    @Test
    @DisplayName("测试相同明文产生不同密文")
    void testDifferentCiphertextForSamePlaintext() throws FieldEncryptor.EncryptionException {
        String plainText = "Same Plain Text";

        String encrypted1 = fieldEncryptor.encrypt(plainText);
        String encrypted2 = fieldEncryptor.encrypt(plainText);

        // 由于每次使用不同的随机IV，密文应该不同
        assertNotEquals(encrypted1, encrypted2);

        // 但解密后应该得到相同的明文
        assertEquals(plainText, fieldEncryptor.decrypt(encrypted1));
        assertEquals(plainText, fieldEncryptor.decrypt(encrypted2));
    }

    @Test
    @DisplayName("测试批量加密")
    void testBatchEncryption() throws FieldEncryptor.EncryptionException {
        String[] plainTexts = {
            "Data 1",
            "数据 2",
            "Information 3"
        };

        String[] encrypted = fieldEncryptor.encryptBatch(plainTexts);
        assertEquals(plainTexts.length, encrypted.length);

        for (int i = 0; i < plainTexts.length; i++) {
            String decrypted = fieldEncryptor.decrypt(encrypted[i]);
            assertEquals(plainTexts[i], decrypted);
        }
    }

    @Test
    @DisplayName("测试批量解密")
    void testBatchDecryption() throws FieldEncryptor.EncryptionException {
        String[] plainTexts = {
            "Data 1",
            "数据 2",
            "Information 3"
        };

        String[] encrypted = new String[plainTexts.length];
        for (int i = 0; i < plainTexts.length; i++) {
            encrypted[i] = fieldEncryptor.encrypt(plainTexts[i]);
        }

        String[] decrypted = fieldEncryptor.decryptBatch(encrypted);
        assertArrayEquals(plainTexts, decrypted);
    }

    @Test
    @DisplayName("测试批量加密null数组")
    void testBatchEncryptionNullArray() {
        FieldEncryptor.EncryptionException exception = assertThrows(
            FieldEncryptor.EncryptionException.class,
            () -> fieldEncryptor.encryptBatch(null)
        );
        assertTrue(exception.getMessage().contains("输入数组不能为空"));
    }

    @Test
    @DisplayName("测试批量解密null数组")
    void testBatchDecryptionNullArray() {
        FieldEncryptor.EncryptionException exception = assertThrows(
            FieldEncryptor.EncryptionException.class,
            () -> fieldEncryptor.decryptBatch(null)
        );
        assertTrue(exception.getMessage().contains("输入数组不能为空"));
    }

    @Test
    @DisplayName("测试加密结果为Base64格式")
    void testEncryptedIsBase64() throws FieldEncryptor.EncryptionException {
        String plainText = "Test Data";
        String encrypted = fieldEncryptor.encrypt(plainText);

        // 验证是否为有效的Base64字符串
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encrypted));
    }

    @Test
    @DisplayName("测试IV长度正确")
    void testIVLength() throws FieldEncryptor.EncryptionException {
        String plainText = "Test Data";
        String encrypted = fieldEncryptor.encrypt(plainText);

        byte[] combined = Base64.getDecoder().decode(encrypted);
        // IV应该是12字节
        assertTrue(combined.length >= 12);
    }

    @Test
    @DisplayName("测试不同密钥产生不同结果")
    void testDifferentKeysProduceDifferentResults() throws FieldEncryptor.EncryptionException {
        String plainText = "Sensitive Information";

        // 使用第一个密钥加密
        FieldEncryptor encryptor1 = new FieldEncryptor();
        ReflectionTestUtils.setField(encryptor1, "encryptionKey", "key1");
        String encrypted1 = encryptor1.encrypt(plainText);

        // 使用第二个密钥加密
        FieldEncryptor encryptor2 = new FieldEncryptor();
        ReflectionTestUtils.setField(encryptor2, "encryptionKey", "key2");
        String encrypted2 = encryptor2.encrypt(plainText);

        // 密文应该不同
        assertNotEquals(encrypted1, encrypted2);

        // 使用错误的密钥解密应该失败
        assertThrows(
            FieldEncryptor.EncryptionException.class,
            () -> encryptor1.decrypt(encrypted2)
        );
    }

    @Test
    @DisplayName("测试数字字符串加密解密")
    void testNumericString() throws FieldEncryptor.EncryptionException {
        String plainText = "1234567890";

        String encrypted = fieldEncryptor.encrypt(plainText);
        String decrypted = fieldEncryptor.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("测试JSON字符串加密解密")
    void testJsonString() throws FieldEncryptor.EncryptionException {
        String plainText = "{\"name\":\"张三\",\"age\":30,\"email\":\"zhangsan@example.com\"}";

        String encrypted = fieldEncryptor.encrypt(plainText);
        String decrypted = fieldEncryptor.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("测试多行文本加密解密")
    void testMultilineText() throws FieldEncryptor.EncryptionException {
        String plainText = "第一行\n第二行\n第三行\n包含换行符的文本";

        String encrypted = fieldEncryptor.encrypt(plainText);
        String decrypted = fieldEncryptor.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("测试Unicode字符加密解密")
    void testUnicodeCharacters() throws FieldEncryptor.EncryptionException {
        String plainText = "Hello 世界 🌍 Привет مرحبا";

        String encrypted = fieldEncryptor.encrypt(plainText);
        String decrypted = fieldEncryptor.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("测试加密性能")
    void testEncryptionPerformance() throws FieldEncryptor.EncryptionException {
        String plainText = "Performance Test Data";
        int iterations = 100;

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String encrypted = fieldEncryptor.encrypt(plainText);
            fieldEncryptor.decrypt(encrypted);
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        System.out.println("100次加密解密耗时: " + duration + "ms");

        // 确保性能在合理范围内（100次操作应该在5秒内完成）
        assertTrue(duration < 5000, "加密解密性能不达标");
    }

    @Test
    @DisplayName("测试并发加密")
    void testConcurrentEncryption() throws InterruptedException {
        String plainText = "Concurrent Test";
        int threadCount = 10;
        Set<String> encryptedResults = new HashSet<>();

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    String encrypted = fieldEncryptor.encrypt(plainText);
                    synchronized (encryptedResults) {
                        encryptedResults.add(encrypted);
                    }
                } catch (FieldEncryptor.EncryptionException e) {
                    fail("并发加密失败: " + e.getMessage());
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // 由于使用随机IV，所有加密结果应该都不同
        assertEquals(threadCount, encryptedResults.size());
    }
}
