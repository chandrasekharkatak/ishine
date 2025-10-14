package com.apmosys.employeeportal;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
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
            "Authorization",
            "authorization",
            "x-trace-map",
            "baggage"
    );

    // Regex patterns for XSS / SQL injection
    private static final Pattern[] MALICIOUS_PATTERNS = new Pattern[]{
    	    // XSS detection (more targeted)
    	    Pattern.compile("<\\s*script\\b", Pattern.CASE_INSENSITIVE),
    	    Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
    	    Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),

    	    // SQL injection (must look like a command, not a word fragment)
    	    Pattern.compile("([';]+\\s*(select|insert|update|delete|drop|union)\\b)", Pattern.CASE_INSENSITIVE),
    	    Pattern.compile("(\\bexec\\b|\\bshutdown\\b|\\bsleep\\b|\\bwaitfor\\b)", Pattern.CASE_INSENSITIVE)
    	};

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        try {
            validateHeaders(wrappedRequest);
            validateParams(wrappedRequest);
//            validateBody(wrappedRequest);

            filterChain.doFilter(wrappedRequest, response);
        } catch (SecurityException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request detected: " + ex.getMessage());
        }
    }

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

    private void validateParams(HttpServletRequest request) {
        request.getParameterMap().forEach((param, values) -> {
            for (String value : values) {
                if (isMalicious(value)) {
                    throw new SecurityException("Malicious value in parameter: " + param);
                }
            }
        });
    }

//    private void validateBody(ContentCachingRequestWrapper request) throws IOException {
//        // Trigger caching by reading the stream
//        request.getParameterMap(); // optional (forces parsing params for form-data)
//        request.getInputStream().readAllBytes(); // read and cache
//
//        byte[] body = request.getContentAsByteArray();
//        if (body.length > 0) {
//            String requestBody = new String(body, request.getCharacterEncoding());
//            if (isMalicious(requestBody)) {
//                throw new SecurityException("Malicious content in request body");
//            }
//        }
//    }


    public static boolean isMalicious(String input) {
        if (input == null) return false;
        for (Pattern pattern : MALICIOUS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }
        return false;
    }
}
