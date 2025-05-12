package com.apmosys.employeeportal.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
@RestController
public class ProxyController {
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);
    private static final String OAP_BASE_URL = "http://192.168.0.75:12800";
    private final RestTemplate restTemplate = new RestTemplate();
    @PostMapping("/browser/perfData")
    public ResponseEntity<String> postPerfData(@RequestBody String entity) {
        logger.info("Proxying /browser/perfData to Apmosys OAP...");
        return proxyToOAP("/browser/perfData", entity);
    }
    @PostMapping("/v3/segments")
    public ResponseEntity<String> postSegments(@RequestBody String entity) {
        logger.info("Proxying /v3/segments to Apmosys OAP...");
        return proxyToOAP("/v3/segments", entity);
    }
    @PostMapping("/browser/errorLog")
    public ResponseEntity<String> postErrorLog(@RequestBody String entity) {
        logger.info("Proxying /browser/errorLog to Apmosys OAP...");
        return proxyToOAP("/browser/errorLog", entity);
    }
    @PostMapping("/browser/errorLogs")
    public ResponseEntity<String> postErrorLogs(@RequestBody String entity) {
        logger.info("Proxying /browser/errorLogs to Apmosys OAP...");
        return proxyToOAP("/browser/errorLogs", entity);
    }
    @PostMapping("/v3/segment")
    public ResponseEntity<String> postSegment(@RequestBody String entity) {
        logger.info("Proxying /v3/segment to Apmosys OAP...");
        return proxyToOAP("/v3/segment", entity);
    }
    @PostMapping("/browser/perfData/webVitals")
    public ResponseEntity<String> postWebVitals(@RequestBody String entity) {
        logger.info("Proxying /browser/perfData/webVitals to Apmosys OAP...");
        return proxyToOAP("/browser/perfData/webVitals", entity);
    }
    
    private ResponseEntity<String> proxyToOAP(String path, String body) {
        String url = OAP_BASE_URL + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, String.class);
            logger.info("OAP Server response status: {}", response.getStatusCode());
            return response;
        } catch (Exception e) {
            logger.error("Error forwarding to OAP Server: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error forwarding request");
        }
    }
}


