package com.apmosys.employeeportal.utility;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
public class EncryptionUtil {

    public static final String KEY1 = "msoe837%)ks&!6eb";   // 16 chars = 128-bit AES key
    public static final String KEY2 = "p10Cu&@m3idh9so5";   // 16 chars = 128-bit IV
    public static final String ENCRYPTED_DATA = "encryptedData";

    /**
     * Encrypt plain text into AES CBC Base64 JSON format: {"encryptedData": "..."}
     */
    public static String encrypt(String plainText) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(KEY2.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec skeySpec = new SecretKeySpec(KEY1.getBytes(StandardCharsets.UTF_8), "AES");

        try {
            if (plainText.contains(ENCRYPTED_DATA)) {
                plainText = extractEncryptedDataValue(plainText);
            }

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ENCRYPTED_DATA, Base64.getEncoder().encodeToString(encryptedBytes));

            return jsonObject.toString();

        } catch (JSONException e) {
            throw new JSONException("JSON error while encrypting: " + e.getMessage());
        } catch (NoSuchPaddingException e) {
            log.error("Encryption failed - No Such Padding", e);
            throw new RuntimeException("Encryption failed - No Such Padding", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Invalid algorithm parameters. Check the encryption.", e);
            throw new NoSuchAlgorithmException("Invalid algorithm parameters for encryption", e);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new Exception("Encryption failed", e);
        }
    }

    /**
     * Decrypt AES CBC Base64 string.
     * Supports both raw Base64 ciphertext and JSON {"encryptedData": "..."}.
     */
    public static String decrypt(String encrypted) throws Exception {
        try {
            if (encrypted.contains(ENCRYPTED_DATA)) {
                encrypted = extractEncryptedDataValue(encrypted);
            }

            IvParameterSpec iv = new IvParameterSpec(KEY2.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY1.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted.getBytes()));

            return new String(original, StandardCharsets.UTF_8);

        } catch (NoSuchPaddingException e) {
            log.error("Decryption failed - No Such Padding", e);
            throw new NoSuchPaddingException("Decryption failed - No Such Padding");
        } catch (NoSuchAlgorithmException e) {
            log.error("Invalid algorithm parameters. Check the decryption.", e);
            throw new NoSuchAlgorithmException("Invalid algorithm parameters for decryption", e);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new Exception("Decryption failed", e);
        }
    }

    /**
     * Extracts the value of "encryptedData" from JSON string
     */
    private static String extractEncryptedDataValue(String encryptedDataInput) {
        try {
            JSONObject jsonObject = new JSONObject(encryptedDataInput);
            return jsonObject.getString(ENCRYPTED_DATA);
        } catch (JSONException e) {
            log.error("Error extracting encryptedData value", e);
            throw new JSONException("Error extracting encryptedData value: " + e.getMessage());
        }
    }

    /**
     * Test run
     */
    public static void main(String[] args) throws Exception {
        String original = "Hello Secure World!";

        // Encrypt
        String encryptedJson = encrypt(original);
        System.out.println("Encrypted JSON: " + encryptedJson);

        // Decrypt
        String decrypted = decrypt("{\"encryptedData\":\"kn7BPQPxuQpSc6ABK9P9Y/dSgtJFdhe8BdK/6mlrcwcurBzBKh6tJr7jOvTe3WggprPjZUAjx6vxQoxN9Hc8tGSlfEpU1Vk69FTzD7C9tC9Ag23hS835+eKZBrFRB6VPLelbQegfcvchObbMUmzKo2bhQPJDOURt1s0nAOCiAFqwM4tYunHuA05eq1h3EQpfV+zZ2+fc4OAMHx5Gh2MHILc8Yp7icWRnthwQO/nc2r1J6jQ+4ydS1+sqJfNpedv4\"}");
        System.out.println("Decrypted text: " + decrypted);
    }
}
