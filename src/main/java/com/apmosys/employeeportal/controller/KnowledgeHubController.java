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
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api/knowledgehub")
public class KnowledgeHubController {

    @Autowired
    private KnowledgeHubService knowledgeHubService;

    @Autowired
    private ProjectInsightProjectFlatSearchService projectInsightProjectFlatSearchService;

    @PostMapping("/onSearchTerm")
    public ResponseEntity<KnowledgeHubSearchResultWrapper> onSearchTerm(
            @RequestBody KnowledgeHubSearchDTO knowledgeHubSearchDTO) {
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

    @PostMapping("/getAllFacetsForKeyword")
    public ServiceResponse getAllFacetsForKeyword(
            @RequestBody KnowledgeHubSearchDTO knowledgeHubSearchDTO) {
        return knowledgeHubService.getAllFacetsForKeyword(knowledgeHubSearchDTO);
    }
}