package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchDTO;
import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchResultWrapper;
import com.apmosys.employeeportal.service.KnowledgeHubService;
import com.apmosys.employeeportal.service.ProjectInsightProjectFlatSearchService;

@RestController
@RequestMapping("/api/knowledgehub")
public class KnowledgeHubController {

    @Autowired
    private KnowledgeHubService knowledgeHubService;

    @Autowired
    private ProjectInsightProjectFlatSearchService projectInsightProjectFlatSearchService;

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query, @RequestParam Integer limit, @RequestParam Integer skip) {
        try {
            return ResponseEntity.ok(knowledgeHubService.getProject(query, limit, skip));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/onSearchTerm")
    public ResponseEntity<KnowledgeHubSearchResultWrapper> onSearchTerm(@RequestBody KnowledgeHubSearchDTO knowledgeHubSearchDTO) {
        return ResponseEntity.ok(knowledgeHubService.onSearchTerm(knowledgeHubSearchDTO));
    }

    @GetMapping("/syncAllDataWithFlatSearch")
    public void syncAllDataWithFlatSearch() {
        projectInsightProjectFlatSearchService.syncAllDataWithFlatSearch();
    }

    @PostMapping("/loadProjectSearchObject")
    public ResponseEntity<?> loadProjectSearchObject(@RequestBody KnowledgeHubSearchDTO knowledgeHubSearchDTO) {
        return ResponseEntity.ok(knowledgeHubService.loadProjectSearchObject(knowledgeHubSearchDTO));
    }
}