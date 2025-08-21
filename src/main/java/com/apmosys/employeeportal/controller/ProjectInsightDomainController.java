package com.apmosys.employeeportal.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.apmosys.employeeportal.dto.DomainDataDTO;
import com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDomainCreatedBy;
import com.apmosys.employeeportal.dto.ProjectInsightDomainDataDto;
import com.apmosys.employeeportal.dto.ProjectInsightEditDomainDTO;
import com.apmosys.employeeportal.model.ProjectInsightDomain;
import com.apmosys.employeeportal.model.ProjectInsightDomainData;
import com.apmosys.employeeportal.repository.ProjectInsightDomainRepository;
import com.apmosys.employeeportal.service.ProjectInsightDomainService;

@RestController
@RequestMapping("/api")
public class ProjectInsightDomainController {

    @Autowired
    private ProjectInsightDomainService projectInsightDomainService;
    
    @Autowired
    private ProjectInsightDomainRepository projectInsightDomainRepository;

    @PostMapping("/create-project-insight-domain")
    public ResponseEntity<?> createProjectInsightDomain(@RequestBody DomainDataDTO projectInsightDomainDTO,
            @RequestParam Long createdBy) {
        try {
            String result = projectInsightDomainService.saveDomainTree(projectInsightDomainDTO,false, createdBy);
            return ResponseEntity.ok(Map.of("message",result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/get-all-project-insight-domains")
    public ResponseEntity<List<ProjectInsightDomainData>> getAllProjectInsightDomains(@RequestBody List<Object> ids) {
        try {
            List<Long> domainIds = new ArrayList<>();
            for (Object idObj : ids) {
                if (idObj instanceof Number) {
                    domainIds.add(((Number) idObj).longValue());
                } else if (idObj instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) idObj;
                    if (map.containsKey("id")) {
                        Object val = map.get("id");
                        if (val instanceof Number) {
                            domainIds.add(((Number) val).longValue());
                        }
                    }
                }
            }

            List<ProjectInsightDomainData> list = projectInsightDomainService.getAllProjectInsightDomains(domainIds);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    @GetMapping("/getAllDomains")
    public ResponseEntity<List<ProjectInsightDomain>> getAllDomainsList(){
    	List<ProjectInsightDomain> response = projectInsightDomainRepository.findAll();
    	return ResponseEntity.ok(response);
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
            @RequestParam(required = false) String createdOn, @RequestParam(required = false) Boolean isActive, @RequestParam(required = false) String isApproved) {
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
            ProjectInsightDomainData result = projectInsightDomainService.findDomain(domain);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/edit-domains")
    public ResponseEntity<?> editDomains(
            @RequestBody DomainDataDTO dto, @RequestParam Long createdBy) {
        try {
            projectInsightDomainService.saveDomainTree(dto, true, createdBy);
            return ResponseEntity.ok(Map.of("message", "Updated Successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete-domain-data")
    public ResponseEntity<?> deleteDomains(@RequestParam Long id, @RequestParam String type) {
        try {
            projectInsightDomainService.softDelete(id);
            return ResponseEntity.ok(Map.of("message", type + " Deleted Successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/approve-domain")
    public ResponseEntity<?> approveDomain(@RequestParam Long id, @RequestParam String isApproved, @RequestParam Long approvedBy) {
        try {
            projectInsightDomainService.approveDomain(id,isApproved, approvedBy);
            return ResponseEntity.ok(Map.of("message", "Approved Successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/filter-project-insight")
    public ResponseEntity<?> filterProjectInsight(@RequestParam Integer page, @RequestParam Integer limit, @RequestBody Map<String, Object> filterProjectInsightDTO) {
        try {
            Page<ProjectInsighProjectMappingDTO> list = projectInsightDomainService
                    .filterProjectInsight(filterProjectInsightDTO,page, limit);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/load-all-filters")
    public ResponseEntity<?> loadAllFilters() {
        try {
            Map<String, Object> map = projectInsightDomainService.loadAllFilters();
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/search-project-insight")
    public ResponseEntity<?> search(@RequestParam String search, @RequestParam Integer page, @RequestParam Integer limit) {
        try {
            Page<ProjectInsighProjectMappingDTO> list = projectInsightDomainService.search(search, page, limit);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/get-domains")
    public ResponseEntity<?> getDomains(@RequestBody List<String> type, @RequestParam(required = false) Long parentId) {
        try {
            List<ProjectInsightDomainDataDto> list = projectInsightDomainService.getDomainsData(type, parentId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/getDomainHierarchy")
    public ResponseEntity<List<List<String>>> getDomainHierarchy() {
        return ResponseEntity.ok(projectInsightDomainService.getDomainHierarchy());
    }
}