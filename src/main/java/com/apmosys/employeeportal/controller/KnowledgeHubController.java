package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.service.KnowledgeHubService;

@RestController
@RequestMapping("/api/knowledgehub")
public class KnowledgeHubController {

    @Autowired
    private KnowledgeHubService knowledgeHubService;

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query, @RequestParam Integer limit, @RequestParam Integer skip) {
        try {
            return ResponseEntity.ok(knowledgeHubService.getProject(query, limit, skip));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}