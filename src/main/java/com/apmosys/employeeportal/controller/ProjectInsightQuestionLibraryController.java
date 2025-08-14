package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.PageDTO;
import com.apmosys.employeeportal.mongodb.dto.ProjectInsightQuestionLibraryEntryRequest;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionLibraryEntry;
import com.apmosys.employeeportal.service.ProjectInsightQuestionLibraryService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ProjectInsightQuestionLibraryController {

        @Autowired
        private ProjectInsightQuestionLibraryService projectInsightQuestionLibraryService;

        @PostMapping("/getAllProjectInsightQuestionEntriesByDepartment")
        public ResponseEntity<Page<ProjectInsightQuestionLibraryEntry>> getAllProjectInsightQuestionEntriesByDepartment(
                        @RequestBody PageDTO pageDTO) {
                return ResponseEntity
                                .ok(projectInsightQuestionLibraryService
                                                .getAllProjectInsightQuestionEntriesByDepartment(pageDTO));
        }

        @PostMapping("/getAllProjectInsightQuestionsEntry")
        public ResponseEntity<Page<ProjectInsightQuestionLibraryEntry>> getAllProjectInsightQuestionsEntry(
                        @RequestBody PageDTO pageDTO) {
                return ResponseEntity
                                .ok(projectInsightQuestionLibraryService.getAllProjectInsightQuestionsEntry(pageDTO));
        }

        @PostMapping("/getAllProjectInsightQuestionEntriesByFilter")
        public ResponseEntity<Page<ProjectInsightQuestionLibraryEntry>> getAllProjectInsightQuestionEntriesByFilter(
                        @RequestBody PageDTO pageDTO) {
                return ResponseEntity
                                .ok(projectInsightQuestionLibraryService
                                                .getAllProjectInsightQuestionEntriesByFilter(pageDTO));
        }

        @PostMapping("/saveProjectInsightQuestionLibraryEntry")
        public ResponseEntity<ServiceResponse> saveProjectInsightQuestionLibraryEntry(
                        @RequestBody ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry) {
                return ResponseEntity
                                .ok(projectInsightQuestionLibraryService
                                                .saveProjectInsightQuestionLibraryEntry(
                                                                projectInsightQuestionLibraryEntry));
        }

        @PostMapping("/deleteProjectInsightQuestionLibraryEntryById")
        public ResponseEntity<ServiceResponse> deleteProjectInsightQuestionLibraryEntryById(
                        @RequestBody ProjectInsightQuestionLibraryEntry projectInsightQuestionLibraryEntry) {
                return ResponseEntity
                                .ok(projectInsightQuestionLibraryService
                                                .deleteProjectInsightQuestionLibraryEntryById(
                                                                projectInsightQuestionLibraryEntry));
        }

        @PostMapping("/saveEntryToQuestionLibraryFromExcel")
        public ResponseEntity<ServiceResponse> saveEntryToQuestionLibraryFromExcel(
                        @RequestBody ProjectInsightQuestionLibraryEntryRequest projectInsightQuestionLibraryEntryRequest) {
                return ResponseEntity
                                .ok(projectInsightQuestionLibraryService
                                                .saveEntryToQuestionLibraryFromExcel(
                                                                projectInsightQuestionLibraryEntryRequest));
        }

        @GetMapping("/searchQuestionLibrary")
        public ResponseEntity<List<ProjectInsightQuestionLibraryEntry>> searchQuestionLibrary(
                        @RequestParam String text) {
                return ResponseEntity.ok(projectInsightQuestionLibraryService.searchQuestionLibrary(text));
        }

        @GetMapping("/getEntryFromsearchQuestionLibraryByText")
        public ResponseEntity<ServiceResponse> getEntryFromsearchQuestionLibraryByText(
                        @RequestParam String id) {
                return ResponseEntity
                                .ok(projectInsightQuestionLibraryService.getEntryFromsearchQuestionLibraryByText(id));
        }

}