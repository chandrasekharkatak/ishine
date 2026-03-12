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
   
}


