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
import java.time.Instant;
import java.util.Base64;

@Slf4j
public class EncryptionUtil {

    public static final String KEY1 = "msoe837%)ks&!6eb";   // 16 chars = 128-bit AES key
    public static final String KEY2 = "p10Cu&@m3idh9so5";   // 16 chars = 128-bit IV
    public static final String ENCRYPTED_DATA = "encryptedData";
    public static final String SALT = "salt"; // timestamp salt

    /**
     * Encrypt plain text into AES CBC Base64 JSON format:
     * {"encryptedData": "...", "salt": 1694959200000}
     */
    public static String encrypt(String plainText) throws Exception {
        long saltTimestamp = Instant.now().toEpochMilli(); // current time in milliseconds

        // Append salt to plaintext before encryption: "actualText|timestamp"
        String saltedPlainText = plainText + "|" + saltTimestamp;

        IvParameterSpec iv = new IvParameterSpec(KEY2.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec skeySpec = new SecretKeySpec(KEY1.getBytes(StandardCharsets.UTF_8), "AES");

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encryptedBytes = cipher.doFinal(saltedPlainText.getBytes(StandardCharsets.UTF_8));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ENCRYPTED_DATA, Base64.getEncoder().encodeToString(encryptedBytes));
            jsonObject.put(SALT, saltTimestamp);

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
     * Decrypt AES CBC Base64 string with ±10s validation (only past timestamps allowed)
     */
    public static String decrypt(String encryptedJson) throws Exception {
        try {
            JSONObject jsonObject = new JSONObject(encryptedJson);
            String encrypted = jsonObject.getString(ENCRYPTED_DATA);
            long saltTimestamp = jsonObject.getLong(SALT);

            long currentTimestamp = Instant.now().toEpochMilli();

            // 1️⃣ Reject if timestamp is in the future
            if (saltTimestamp > currentTimestamp) {
                throw new SecurityException("Decryption rejected: timestamp is in the future");
            }

            // 2️⃣ Reject if timestamp is older than 10s
            long diff = currentTimestamp - saltTimestamp;
            if (diff > 10_000) { // 10 seconds = 10,000 ms
                throw new SecurityException("Decryption rejected: timestamp expired (" + diff + "ms old)");
            }

            IvParameterSpec iv = new IvParameterSpec(KEY2.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY1.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

            String decryptedWithSalt = new String(original, StandardCharsets.UTF_8);

            // Remove salt (split at last "|")
            int lastPipeIndex = decryptedWithSalt.lastIndexOf("|");
            if (lastPipeIndex < 0) {
                throw new SecurityException("Invalid decrypted format: missing salt delimiter");
            }

            return decryptedWithSalt.substring(0, lastPipeIndex);

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
     * Test run
     */
    public static void main(String[] args) throws Exception {
        String original = "Hello Secure World!";

        // Encrypt
        String encryptedJson = encrypt(original);
        System.out.println("Encrypted JSON: " + encryptedJson);

        // Decrypt
        String decrypted = decrypt(encryptedJson);
        System.out.println("Decrypted text: " + decrypted);
    }
}
