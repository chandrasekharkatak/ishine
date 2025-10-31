package com.apmosys.employeeportal;

import com.apmosys.employeeportal.utility.EncryptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collections;

@Slf4j
@ControllerAdvice
public class EncryptionResponseBodyAdvice implements ResponseBodyAdvice<Object> {

//    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TRACE_HEADER = "X-TRACE-MAP";

    private final ObjectMapper objectMapper;

    public EncryptionResponseBodyAdvice() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()); // <-- Fix for LocalDateTime
    }
    
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(Encrypted.class) ||
               returnType.getDeclaringClass().isAnnotationPresent(Encrypted.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        try {
            // --- 1. Convert response body to JSON string ---
        	System.out.println(body);
        	Object actualBody = body;
        	if (body instanceof Optional) {
        	    Optional<?> optional = (Optional<?>) body;
        	    if (optional.isPresent()) {
        	        actualBody = optional.get();
        	    } else {
        	        actualBody = Collections.emptyMap(); // empty response fallback
        	    }
        	}
        	
            String json = objectMapper.writeValueAsString(actualBody);
            System.out.println(json);
            // --- 2. Get traceMap header ---
            String traceHeader = request.getHeaders().getFirst(TRACE_HEADER);
            if (traceHeader == null) {
                log.warn("No X-TRACE-MAP header found for encryption. Sending plain body.");
                return body;
            }

            JSONObject traceMap = EncryptionUtil.decryptTraceMap(traceHeader);

            // --- 3. Find the matching key in traceMap ---
            String matchedKey = null;

            String requestPath = EncryptionUtil.getResponseURI(); 
            System.out.println(requestPath);
            // Note: implement getRequestPath() to return normalized path of the current request
            String traceId = null;
            for (String key : traceMap.keySet()) {
            	System.out.println(key.endsWith(requestPath));
            	if (requestPath.endsWith(key) || key.endsWith(requestPath)){
            		matchedKey = key;
            		traceId = traceMap.getString(key); break;
            	}
            }
            if (traceId == null) {
                log.warn("TraceId not found for API: {}. Sending plain body.", requestPath);
                return body;
            }

            // --- 4. Encrypt response body using traceId ---
            String encryptedResponse = EncryptionUtil.encrypt(json, traceId);

            // --- 5. Set the same encrypted traceMap header back ---
            response.getHeaders().set(TRACE_HEADER,
                    EncryptionUtil.encryptTraceMap(Collections.singletonMap(matchedKey, traceId)));

            return encryptedResponse;

        } catch (Exception e) {
            log.error("Response encryption failed", e);
            return body; // fallback to plain response
        }
    }
}
