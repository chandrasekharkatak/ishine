package com.apmosys.employeeportal.controller;


import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@RestController
public class ProxyController {

    private final String SKY_WALKING_BASE_URL = "http://192.168.0.75:12800";

    private final RestTemplate restTemplate = new RestTemplate();

    @RequestMapping({"browser/perfData", "browser/errorLog", "browser/errorLogs", "v3/segment", "v3/segments"})
    public ResponseEntity<byte[]> proxyToSkyWalking(HttpServletRequest request,
                                                    @RequestBody(required = false) byte[] body,
                                                    @RequestHeader HttpHeaders headers) {

        String fullUrl = SKY_WALKING_BASE_URL;

        HttpHeaders forwardedHeaders = new HttpHeaders();
        headers.forEach(forwardedHeaders::put);

        HttpEntity<byte[]> entity = new HttpEntity<>(body, forwardedHeaders);
        ResponseEntity<byte[]> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                byte[].class
        );

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }
}






