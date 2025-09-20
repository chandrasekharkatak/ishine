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

    /**
     * Encrypt plain text into AES CBC Base64 JSON format:
     * {"encryptedData": "..."} but salt is embedded in encrypted payload
     */
    public static String encrypt(String plainText) throws Exception {
        long saltTimestamp = Instant.now().toEpochMilli(); // current time in ms

        // Append salt to plaintext: "actualText|timestamp"
        String saltedPlainText = plainText + "|" + saltTimestamp;

        IvParameterSpec iv = new IvParameterSpec(KEY2.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec skeySpec = new SecretKeySpec(KEY1.getBytes(StandardCharsets.UTF_8), "AES");

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encryptedBytes = cipher.doFinal(saltedPlainText.getBytes(StandardCharsets.UTF_8));

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
     * Decrypt AES CBC Base64 string with ±10s validation (only past timestamps allowed)
     */
    public static String decrypt(String encryptedJson) throws Exception {
        try {
            JSONObject jsonObject = new JSONObject(encryptedJson);
            String encrypted = jsonObject.getString(ENCRYPTED_DATA);

            IvParameterSpec iv = new IvParameterSpec(KEY2.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY1.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

            String decryptedWithSalt = new String(original, StandardCharsets.UTF_8);

            // Extract plaintext + salt
            int lastPipeIndex = decryptedWithSalt.lastIndexOf("|");
            if (lastPipeIndex < 0) {
                throw new SecurityException("Invalid decrypted format: missing salt delimiter");
            }

            String plaintext = decryptedWithSalt.substring(0, lastPipeIndex);
            long saltTimestamp = Long.parseLong(decryptedWithSalt.substring(lastPipeIndex + 1));

            long currentTimestamp = Instant.now().toEpochMilli();

            // Validate timestamp
            if (saltTimestamp > currentTimestamp) {
                throw new SecurityException("Decryption rejected: timestamp is in the future");
            }

            long diff = currentTimestamp - saltTimestamp;
            if (diff > 30_000) { 
                throw new SecurityException("Decryption rejected: timestamp expired (" + diff + "ms old)");
            }

            return plaintext;

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

    public static void main(String[] args) throws Exception {
        String original = "Hello Secure World!";

        String encryptedJson = encrypt(original);
        System.out.println("Encrypted JSON: " + encryptedJson);

        String decrypted = decrypt(encryptedJson);
        System.out.println("Decrypted text: " + decrypted);
    }
}
