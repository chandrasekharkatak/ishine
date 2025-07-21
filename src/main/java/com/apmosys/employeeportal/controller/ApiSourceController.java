package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.apmosys.employeeportal.dto.ActivityTemplateDTO;
import com.apmosys.employeeportal.dto.ApiSourceDTO;
import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.HierarchyOptionDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDomainDTO;
import com.apmosys.employeeportal.service.ActivityTemplateService;
import com.apmosys.employeeportal.service.ApiSourceService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.apmosys.employeeportal.dto.ClientIdAndName;
import com.apmosys.employeeportal.dto.ProjectIdAndNameDTO;
import com.apmosys.employeeportal.dto.ProjectManagerIdAndNameDTO;
import com.apmosys.employeeportal.model.DeliveryMode;
import com.apmosys.employeeportal.model.Outcomes;
import com.apmosys.employeeportal.model.TechStack;
import com.apmosys.employeeportal.service.ProjectService;

@RestController
@RequestMapping("/api")
public class ApiSourceController {

    @Autowired
    ApiSourceService apiSourceService;

    @RequestMapping(value = "/getAllApiList", method = RequestMethod.GET)
    public ResponseEntity<List<ApiSourceDTO>> getAllApiList() {
        List<ApiSourceDTO> response = apiSourceService.getAllApiList();
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/getAllProject", method = RequestMethod.GET)
    public ResponseEntity<List<ProjectDTO>> getAllProject() {
        List<ProjectDTO> response = apiSourceService.getAllProject();
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/getClientByProjectId/{projectId}", method = RequestMethod.GET)
    public ResponseEntity<List<ProjectDTO>> getClientByProjectId(@PathVariable String projectId) {
        List<ProjectDTO> response = apiSourceService.getClientByProjectId(projectId);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/getAllDepartment", method = RequestMethod.GET)
    public ResponseEntity<List<DepartmentDTO>> getAllDepartment() {
        List<DepartmentDTO> response = apiSourceService.getAllDepartment();
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/getAllEmployee", method = RequestMethod.GET)
    public ResponseEntity<List<EmployeeDTO>> getAllEmployee() {
        List<EmployeeDTO> response = apiSourceService.getAllEmployee();
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/getAllProjectInsightDomain", method = RequestMethod.GET)
    public ResponseEntity<List<HierarchyOptionDTO>> getAllDomain() {
        List<HierarchyOptionDTO> response = apiSourceService.getAllDomain();
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/getAllNextFieldAndOption/{type}/{id}", method = RequestMethod.GET)
    public ResponseEntity<List<HierarchyOptionDTO>> getAllNextFieldAndOption(@PathVariable String id,
            @PathVariable String type) {
        List<HierarchyOptionDTO> response = apiSourceService.getAllNextFieldAndOption(id, type);
        return ResponseEntity.ok(response);
    }

    @Autowired
    private ProjectService projectService;

    @GetMapping("/get-all-project-name")
    public ResponseEntity<?> getAllProjectName() {
        try {
            List<ProjectIdAndNameDTO> projectList = projectService.getAllProjectByIdAndName();
            return ResponseEntity.ok(projectList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/get-all-project-managers")
    public ResponseEntity<?> getAllProjectManagers() {
        try {
            List<ProjectManagerIdAndNameDTO> projectList = projectService.getAllProjectManagerById(1206L);
            System.out.println(projectList);
            return ResponseEntity.ok(projectList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }

    @GetMapping("/get-all-clients")
    public ResponseEntity<?> getAllClients() {
        try {
            List<ClientIdAndName> projectList = projectService.findAllClientIdAndName();
            return ResponseEntity.ok(projectList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }

    @GetMapping("/get-all-team-leads")
    public ResponseEntity<?> getAllTeamLeads() {
        try {
            List<ProjectManagerIdAndNameDTO> projectList = projectService.findAllTeamLeads(2);
            return ResponseEntity.ok(projectList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }

    @GetMapping("/get-all-tech-stack")
    public ResponseEntity<?> getAllTechStack() {
        try {
            List<TechStack> projectList = projectService.getAllTechStack();
            return ResponseEntity.ok(projectList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }

    @GetMapping("/get-all-outcomes")
    public ResponseEntity<?> getAllOutcomes() {
        try {
            List<Outcomes> outcomesList = projectService.getAllOutcomes();
            return ResponseEntity.ok(outcomesList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }

    @GetMapping("/get-all-delivery-mode")
    public ResponseEntity<?> getAllDeliveryModes() {
        try {
            List<DeliveryMode> deliveryModes = projectService.getAllDeliveryModes();
            System.out.println("deliveryModes  " + deliveryModes);
            return ResponseEntity.ok(deliveryModes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }

    @GetMapping("/get-all-domain-with-projects")
    public ResponseEntity<?> getAllDomainWithProjects() {
        try {
            Map<String, Set<String>> projectList = apiSourceService.findIdsWithDomainKey();
            return ResponseEntity.ok(projectList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }

    // @GetMapping("/get-all-projects")
    // public ResponseEntity<?> getAllProjects() {
    // try {
    // List<ProjectIdAndNameDTO> projectList =
    // projectService.getAllProjectByIdAndName();
    // return ResponseEntity.ok(projectList);
    // } catch (Exception e) {
    // e.printStackTrace();
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal
    // Server Error");
    // }
    // }

}