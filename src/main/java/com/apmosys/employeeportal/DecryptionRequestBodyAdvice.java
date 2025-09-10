package com.apmosys.employeeportal;

import com.apmosys.employeeportal.utility.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@Slf4j
@ControllerAdvice
public class DecryptionRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        try {
            // Read encrypted request
        	if (parameter.hasMethodAnnotation(Encrypted.class) ||
        	        parameter.getContainingClass().isAnnotationPresent(Encrypted.class)) {
            String encryptedJson = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);
            log.info("🔒 Received Encrypted JSON: {}", encryptedJson);
           
//            if (RequestValidationFilter.isMalicious(encryptedJson)) {
//                throw new SecurityException("Malicious content in encrypted body");
//            }
            // Decrypt
            String decrypted = EncryptionUtil.decrypt(encryptedJson);
            log.info("🔓 Decrypted JSON: {}", decrypted);

            if (RequestValidationFilter.isMalicious(decrypted)) {
                throw new SecurityException("Malicious content in decrypted body");
            }
            byte[] decryptedBytes = decrypted.getBytes(StandardCharsets.UTF_8);

            return new HttpInputMessage() {
                @Override
                public InputStream getBody() {
                    return new ByteArrayInputStream(decryptedBytes);
                }

                @Override
                public HttpHeaders getHeaders() {
                    return inputMessage.getHeaders();
                }
            };
        }else {
            String rawBody = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);

            log.debug("➡️ Non-encrypted request detected for: {}", parameter.getMethod().getName());

            if (RequestValidationFilter.isMalicious(rawBody)) {
                throw new SecurityException("Malicious content in plain body");
            }

            // Pass original (unchanged) body back
            byte[] originalBytes = rawBody.getBytes(StandardCharsets.UTF_8);
//            return wrapBytes(inputMessage, originalBytes);
            return new HttpInputMessage() {
                @Override
                public InputStream getBody() {
                    return new ByteArrayInputStream(originalBytes);
                }
                @Override
                public HttpHeaders getHeaders() {
                    return inputMessage.getHeaders();
                }
            };
        }
        } catch (SecurityException e) {
            log.error("❌ Malicious content detected", e);
            // Stop processing immediately and return 400 Bad Request
            throw new org.springframework.http.converter.HttpMessageNotReadableException(
                    "Invalid request: " + e.getMessage(), e, inputMessage);
        } catch (Exception e) {
            log.error("❌ Request decryption failed", e);
            // Stop processing immediately and return 400 Bad Request
            throw new org.springframework.http.converter.HttpMessageNotReadableException(
                    "Failed to decrypt request body", e, inputMessage);
        }
    }
}
