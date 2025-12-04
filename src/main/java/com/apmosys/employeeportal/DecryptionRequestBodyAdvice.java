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
            // Read raw request body
            String rawBody = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);

            String processedBody; // Will hold either decrypted or original body

            // --- Check if @Encrypted annotation is present ---
            if (parameter.hasMethodAnnotation(Encrypted.class) ||
                parameter.getContainingClass().isAnnotationPresent(Encrypted.class)) {

//                log.info("Received Encrypted JSON: {}", rawBody);

                // 1. Get trace map header
                String traceHeader = inputMessage.getHeaders().getFirst(TRACE_HEADER);
                if (traceHeader == null) {
                    throw new SecurityException("Missing X-TRACE-MAP header");
                }

                JSONObject traceMap = EncryptionUtil.decryptTraceMap(traceHeader);
//                log.debug("Decrypted trace map: {}", traceMap);

                // 2. Resolve traceId for the request path
                String requestPath = EncryptionUtil.getRequestPath();
                String traceId = null;
                for (String key : traceMap.keySet()) {
                    if (requestPath.endsWith(key) || key.endsWith(requestPath)) {
                        traceId = traceMap.getString(key);
                        break;
                    }
                }

                if (traceId == null) {
                    throw new SecurityException("TraceId not found for API: " + requestPath);
                }

                // 3. Decrypt request body
                processedBody = EncryptionUtil.decrypt(rawBody, traceId);
//                log.info("Decrypted JSON: {}", processedBody);

            } else {
                // Non-encrypted request, use raw body
                processedBody = rawBody;
            }

            // --- Centralized malicious check ---
            if (RequestValidationFilter.isMalicious(processedBody)) {
                throw new SecurityException("Malicious content in request body");
            }

            // Convert processed body to InputStream
            byte[] bodyBytes = processedBody.getBytes(StandardCharsets.UTF_8);
            return new HttpInputMessage() {
                @Override
                public InputStream getBody() {
                    return new ByteArrayInputStream(bodyBytes);
                }

                @Override
                public HttpHeaders getHeaders() {
                    return inputMessage.getHeaders();
                }
            };

        } catch (Exception e) {
            log.error("Request processing failed", e);
            throw new HttpMessageNotReadableException("Failed to read/decrypt request body", e, inputMessage);
        }
    }
}
