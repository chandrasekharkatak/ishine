package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
@RequestMapping("/api")
public class ApiSourceController {
	
	@Autowired
	ApiSourceService apiSourceService;

	@RequestMapping( value = "/getAllApiList", method = RequestMethod.GET)
    public ResponseEntity<List<ApiSourceDTO>> getAllApiList() {
        List<ApiSourceDTO> response = apiSourceService.getAllApiList();
        return ResponseEntity.ok(response);
    }
	
	@RequestMapping( value = "/getAllProject", method = RequestMethod.GET)
    public ResponseEntity<List<ProjectDTO>> getAllProject() {
        List<ProjectDTO> response = apiSourceService.getAllProject();
        return ResponseEntity.ok(response);
    }
	
	@RequestMapping( value = "/getClientByProjectId/{projectId}", method = RequestMethod.GET)
    public ResponseEntity<List<ProjectDTO>> getClientByProjectId(@PathVariable String projectId) {
        List<ProjectDTO> response = apiSourceService.getClientByProjectId(projectId);
        return ResponseEntity.ok(response);
    }
	
	@RequestMapping( value = "/getAllDepartment", method = RequestMethod.GET)
    public ResponseEntity<List<DepartmentDTO>> getAllDepartment() {
        List<DepartmentDTO> response = apiSourceService.getAllDepartment();
        return ResponseEntity.ok(response);
    }
	
	@RequestMapping( value = "/getAllEmployee", method = RequestMethod.GET)
    public ResponseEntity<List<EmployeeDTO>> getAllEmployee() {
        List<EmployeeDTO> response = apiSourceService.getAllEmployee();
        return ResponseEntity.ok(response);
    }
	
	@RequestMapping( value = "/getAllProjectInsightDomain", method = RequestMethod.GET)
    public ResponseEntity<List<HierarchyOptionDTO>> getAllDomain() {
        List<HierarchyOptionDTO> response = apiSourceService.getAllDomain();
        return ResponseEntity.ok(response);
    }
	
	@RequestMapping( value = "/getAllNextFieldAndOption/{type}/{id}", method = RequestMethod.GET)
    public ResponseEntity<List<HierarchyOptionDTO>> getAllNextFieldAndOption(@PathVariable String id,@PathVariable String type ) {
        List<HierarchyOptionDTO> response = apiSourceService.getAllNextFieldAndOption(id, type);
        return ResponseEntity.ok(response);
    }

}
