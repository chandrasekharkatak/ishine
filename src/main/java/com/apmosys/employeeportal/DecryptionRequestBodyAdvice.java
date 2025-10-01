package com.apmosys.employeeportal;

import com.apmosys.employeeportal.utility.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    private static final String TRACE_HEADER = "X-TRACE-MAP";

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // intercept all requests
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        try {
            String rawBody = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);

            if (parameter.hasMethodAnnotation(Encrypted.class) ||
                parameter.getContainingClass().isAnnotationPresent(Encrypted.class)) {

                log.info("Received Encrypted JSON: {}", rawBody);

                // --- 1. Read trace map header ---
                String traceHeader = inputMessage.getHeaders().getFirst(TRACE_HEADER);
                if (traceHeader == null) {
                    throw new SecurityException("Missing X-TRACE-MAP header");
                }

                JSONObject traceMap = EncryptionUtil.decryptTraceMap(traceHeader);
                log.debug("Decrypted trace map: {}", traceMap);

                // --- 2. Get request path from current HttpInputMessage ---
                String requestPath = EncryptionUtil.getRequestPath(); 
                System.out.println(requestPath);
                // Note: implement getRequestPath() to return normalized path of the current request
                String traceId = null;
                for (String key : traceMap.keySet()) {
                	System.out.println(key);
                	if (requestPath.endsWith(key) || key.endsWith(requestPath)){
                		traceId = traceMap.getString(key); break;
                	}
                }
//                String traceId = traceMap.optString(requestPath, null);
                
                if (traceId == null) {
                    throw new SecurityException("TraceId not found for API: " + requestPath);
                }

                // --- 3. Decrypt body using traceId ---
                String decrypted = EncryptionUtil.decrypt(rawBody, traceId);
                log.info("Decrypted JSON: {}", decrypted);

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
            } else {
                // Non-encrypted request, just pass through
                byte[] originalBytes = rawBody.getBytes(StandardCharsets.UTF_8);
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
        } catch (Exception e) {
            log.error("Request decryption failed", e);
            throw new HttpMessageNotReadableException("Failed to decrypt request body", e, inputMessage);
        }
    }
}
