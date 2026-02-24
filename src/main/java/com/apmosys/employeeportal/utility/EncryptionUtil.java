package com.apmosys.employeeportal.utility;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.apmosys.employeeportal.RequestValidationFilter;

import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

@Slf4j
public class EncryptionUtil {

    public static final String KEY1 = "msoe837%)ks&!6eb";   // 16 chars = 128-bit AES key
    public static final String KEY2 = "p10Cu&@m3idh9so5";   // 16 chars = 128-bit IV
    public static final String ENCRYPTED_DATA = "encryptedData";

    /**
     * Encrypt payload with optional traceId as salt
     */
    public static String encrypt(String plainText, String traceId) throws Exception {
        String salted = traceId != null ? plainText + "|" + traceId : plainText;

        IvParameterSpec iv = new IvParameterSpec(KEY2.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec skeySpec = new SecretKeySpec(KEY1.getBytes(StandardCharsets.UTF_8), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        byte[] encryptedBytes = cipher.doFinal(salted.getBytes(StandardCharsets.UTF_8));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ENCRYPTED_DATA, Base64.getEncoder().encodeToString(encryptedBytes));

        return jsonObject.toString();
    }

    /**
     * Decrypt payload with optional traceId verification
     */
    public static String decrypt(String encryptedJson, String traceId) throws Exception {
//    	System.out.println(encryptedJson);
        JSONObject jsonObject = new JSONObject(encryptedJson);
        String encrypted = jsonObject.getString(ENCRYPTED_DATA);

        IvParameterSpec iv = new IvParameterSpec(KEY2.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec skeySpec = new SecretKeySpec(KEY1.getBytes(StandardCharsets.UTF_8), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        String decryptedWithTrace = new String(original, StandardCharsets.UTF_8);

        if (traceId != null) {
            int lastPipe = decryptedWithTrace.lastIndexOf('|');
            if (lastPipe < 0) throw new SecurityException("Invalid request format, missing traceId");
            String bodyTraceId = decryptedWithTrace.substring(lastPipe + 1);

            if (!traceId.equals(bodyTraceId)) {
                throw new SecurityException("TraceId mismatch! Potential tampering detected.");
            }

            return decryptedWithTrace.substring(0, lastPipe);
        } else {
            // No traceId to verify (login or unsecure endpoint)
            return decryptedWithTrace;
        }
    }

    /**
     * Encrypt traceMap header for frontend verification
     */
    public static String encryptTraceMap(Map<String, String> traceMap) throws Exception {
        return encrypt(new JSONObject(traceMap).toString(), null);
    }

    /**
     * Decrypt traceMap header
     */
    public static JSONObject decryptTraceMap(String encryptedHeader) throws Exception {
        String decrypted = decrypt(encryptedHeader, null);
        return new JSONObject(decrypted);
    }
    
    /**
     * Returns the normalized request path of the current HTTP request.
     * Example: /api/getEmployeeById
     */
    public static String getRequestPath() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("No current request available");
        }

        HttpServletRequest request = attrs.getRequest();
        String path = request.getRequestURI(); // includes context path
        String contextPath = request.getContextPath(); // usually ""
        
        // Remove context path if present to normalize
        if (contextPath != null && !contextPath.isEmpty()) {
            path = path.substring(contextPath.length());
        }

        return path;
    }
    /**
     * Returns the URI associated with the current response.
     * Example: /api/getEmployeeById
     */
    public static String getResponseURI() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            throw new IllegalStateException("No current request/response available");
        }

        HttpServletRequest request = attrs.getRequest();
        String path = request.getRequestURI(); // includes context path
        String contextPath = request.getContextPath(); // usually ""
        
        // Remove context path if present to normalize
        if (contextPath != null && !contextPath.isEmpty()) {
            path = path.substring(contextPath.length());
        }

        return path;
    }
    
    public static String decryptMinor(String cipherText) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(cipherText); // decode Base64

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(KEY1.getBytes("UTF-8"), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(KEY2.getBytes("UTF-8"));
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decrypted = cipher.doFinal(decoded);
        String data = new String(decrypted, "UTF-8");
        if (RequestValidationFilter.isMalicious(data)) {
            throw new SecurityException("Malicious content in request body");
        }
        return data;
    }

    public static void main(String[] args) throws Exception {
    	 String data = decryptMinor("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");
    	 System.out.println(data);
    }}