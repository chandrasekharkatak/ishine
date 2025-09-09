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
        return methodParameter.hasMethodAnnotation(Encrypted.class) ||
               methodParameter.getContainingClass().isAnnotationPresent(Encrypted.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        try {
            // Read encrypted request
            String encryptedJson = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);
            log.info("🔒 Received Encrypted JSON: {}", encryptedJson);

            // Decrypt
            String decrypted = EncryptionUtil.decrypt(encryptedJson);
            log.info("🔓 Decrypted JSON: {}", decrypted);

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
        } catch (Exception e) {
            log.error("❌ Request decryption failed", e);
            return inputMessage; // fallback to original
        }
    }
}
