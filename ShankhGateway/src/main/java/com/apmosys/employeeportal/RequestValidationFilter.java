package com.apmosys.employeeportal;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RequestValidationFilter extends OncePerRequestFilter {

    // Headers that often contain special characters (don’t block them)
    private static final Set<String> SAFE_HEADERS = Set.of(
            "user-agent",
            "accept-language",
            "accept-encoding",
            "cookie",
            "connection",
            "host",
            "cache-control",
            "authorization",
            "x-trace-map",
            "baggage"
    );

    // Regex patterns for XSS / SQL injection
    private static final Pattern[] MALICIOUS_PATTERNS = new Pattern[]{
    	    // XSS detection (HTML/script injection)
    	    Pattern.compile("<\\s*script\\b", Pattern.CASE_INSENSITIVE),
    	    Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
    	    Pattern.compile("<[^>]*\\s+on\\w+\\s*=", Pattern.CASE_INSENSITIVE), // FIXED here

    	    // SQL injection (commands, not fragments)
    	    Pattern.compile("([';]+\\s*(select|insert|update|delete|drop|union)\\b)", Pattern.CASE_INSENSITIVE),
    	    Pattern.compile("(\\bexec\\b|\\bshutdown\\b|\\bsleep\\b|\\bwaitfor\\b)", Pattern.CASE_INSENSITIVE)
    	};


    // Fields that may contain base64/encrypted data — skip these from validation
    private static final Set<String> WHITELIST_FIELDS = new HashSet<>(Arrays.asList(
            "password", "token", "encdata", "encryptedpassword", "signature"
    ));

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        try {
            validateHeaders(wrappedRequest);
            validateParams(wrappedRequest);
            // Optionally enable this once tested:
            // validateBody(wrappedRequest);

            filterChain.doFilter(wrappedRequest, response);
        } catch (SecurityException ex) {
            logger.error("❌ Malicious content detected: " + ex.getMessage(), ex);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request detected: " + ex.getMessage());
        }
    }

    // ---------------- HEADER VALIDATION ----------------
    private void validateHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            String value = request.getHeader(header);

            if (value != null && !SAFE_HEADERS.contains(header.toLowerCase())) {
                if (isMalicious(value)) {
                    throw new SecurityException("Malicious value in header: " + header);
                }
            }
        }
    }

    // ---------------- PARAM VALIDATION ----------------
    private void validateParams(HttpServletRequest request) {
        request.getParameterMap().forEach((param, values) -> {
            // skip whitelisted params like password/token
            if (WHITELIST_FIELDS.contains(param.toLowerCase())) return;

            for (String value : values) {
                if (looksLikeBase64(value)) continue; // skip likely encrypted/base64 fields

                if (isMalicious(value)) {
                    logger.warn("⚠️ Potential attack vector detected in parameter: " + param + " => " + value);
                    throw new SecurityException("Malicious value in parameter: " + param);
                }
            }
        });
    }

    // ---------------- BODY VALIDATION (optional) ----------------
    /*
    private void validateBody(ContentCachingRequestWrapper request) throws IOException {
        request.getParameterMap(); // triggers caching for form-data
        request.getInputStream().readAllBytes(); // ensure cached content

        byte[] body = request.getContentAsByteArray();
        if (body.length > 0) {
            String requestBody = new String(body, request.getCharacterEncoding());
            if (isMalicious(requestBody)) {
                throw new SecurityException("Malicious content in request body");
            }
        }
    }
    */

    // ---------------- UTILITY METHODS ----------------

    public static boolean isMalicious(String input) {
        if (input == null || input.isEmpty()) return false;

        for (Pattern pattern : MALICIOUS_PATTERNS) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                // Log which pattern matched
                System.err.println("Matched pattern: " + pattern.pattern() + " on value: " + input);
                return true;
            }
        }
        return false;
    }

    // Detect likely base64/encrypted values to avoid false positives
    private static boolean looksLikeBase64(String value) {
        return value != null && value.length() > 8 && value.matches("^[A-Za-z0-9+/]+={0,2}$");
    }
}
