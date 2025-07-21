package com.apmosys.employeeportal.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import com.apmosys.employeeportal.dto.ProjectInsightDomainCreatedBy;
import com.apmosys.employeeportal.dto.ProjectInsightDomainDTO;
import com.apmosys.employeeportal.dto.ProjectInsightEditDomainDTO;
import com.apmosys.employeeportal.enums.ProjectInsightDomainApprovedStatus;
import com.apmosys.employeeportal.model.ProjectInsightDomain;
import com.apmosys.employeeportal.model.ProjectInsightSubDomain;
import com.apmosys.employeeportal.service.ProjectInsightDomainService;

@RestController
@RequestMapping("/api")
public class ProjectInsightDomainController {

    @Autowired
    private ProjectInsightDomainService projectInsightDomainService;

    @PostMapping("/create-project-insight-domain")
    public ResponseEntity<?> createProjectInsightDomain(@RequestBody ProjectInsightDomainDTO projectInsightDomainDTO,
            @RequestParam Long createdBy) {
        try {
            Map<String, Object> result = projectInsightDomainService.createProjectInsightDomain(projectInsightDomainDTO, createdBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/get-all-project-insight-domains")
    public ResponseEntity<List<ProjectInsightDomain>> getAllProjectInsightDomains(@RequestBody List<Long> ids) {
        try {
            List<ProjectInsightDomain> list = projectInsightDomainService.getAllProjectInsightDomains(ids);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/add-new-sub-domain")
    public ResponseEntity<?> addNewSubDomain(@RequestBody ProjectInsightEditDomainDTO projectInsightEditDomainDTO) {
        try {
            Long result = projectInsightDomainService.addNewData(projectInsightEditDomainDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-project-domain")
    public ResponseEntity<?> getProjectDomain(@RequestParam Integer page, @RequestParam Integer limit,
            @RequestParam(required = false) String createdBy, @RequestParam(required = false) String domain,
            @RequestParam(required = false) String createdOn, @RequestParam(required = false) Boolean isActive, @RequestParam(required = false) ProjectInsightDomainApprovedStatus isApproved) {
        try {
            LocalDateTime parsedDate = null;
            if (createdOn != null) {
                // Try ISO format first
                try {
                    parsedDate = LocalDateTime.parse(createdOn, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e1) {
                    // Try without T separator
                    try {
                        parsedDate = LocalDateTime.parse(createdOn,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (DateTimeParseException e2) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Invalid date format. Use YYYY-MM-DDTHH:mm:ss or YYYY-MM-DD HH:mm:ss");
                    }
                }
            }
            Page<ProjectInsightDomainCreatedBy> result = projectInsightDomainService.findAllDomainSearched(domain,
                    createdBy,
                    parsedDate, isActive, page, limit, isApproved);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-domain")
    public ResponseEntity<?> getDomain(@RequestParam String domain) {
        try {
            ProjectInsightDomain result = projectInsightDomainService.findDomain(domain);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/edit-domains")
    public ResponseEntity<?> editDomains(
            @RequestBody List<ProjectInsightEditDomainDTO> listProjectInsightEditDomainDTO) {
        try {
            projectInsightDomainService.editDomains(listProjectInsightEditDomainDTO);
            return ResponseEntity.ok(Map.of("message", "Updated Successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete-domain-data")
    public ResponseEntity<?> deleteDomains(@RequestParam Long id, @RequestParam String type,
            @RequestParam(required = false) String name) {
        try {
            projectInsightDomainService.softDelete(id, type, name);
            return ResponseEntity.ok(Map.of("message", type + " Deleted Successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/approved-domain")
    public ResponseEntity<?> approveDomain(@RequestParam Long id, @RequestParam ProjectInsightDomainApprovedStatus isApproved, @RequestParam Long approvedBy) {
        try {
            projectInsightDomainService.approveDomain(id, isApproved, approvedBy);
            return ResponseEntity.ok(Map.of("message", "Approved Successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}