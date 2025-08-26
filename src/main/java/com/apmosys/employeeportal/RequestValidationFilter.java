package com.apmosys.employeeportal;


import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            "cache-control"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            validateHeaders(request);
            validateParams(request);
            // 🔒 You can also add request body validation if needed

            filterChain.doFilter(request, response);
        } catch (SecurityException ex) {
            // Block suspicious request
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request detected: " + ex.getMessage());
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

    // Basic malicious input detection (XSS / SQLi patterns)
    private boolean isMalicious(String input) {
        if (input == null) return false;

        String lower = input.toLowerCase();

        // XSS attempt
        if (lower.contains("<script") || lower.contains("</script") || lower.contains("javascript:")) {
            return true;
        }

        // SQL injection attempt
        if (lower.contains("select ") || lower.contains("union ") || lower.contains("insert ")
                || lower.contains("update ") || lower.contains("delete ") || lower.contains("drop ")) {
            return true;
        }

        return false;
    }
}
