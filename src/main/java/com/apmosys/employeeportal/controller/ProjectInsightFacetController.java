package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.ProjectInsightFacetCategoryDTO;
import com.apmosys.employeeportal.model.ProjectInsightFacetCategory;
import com.apmosys.employeeportal.service.ProjectInsightFacetService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class ProjectInsightFacetController {

	private ProjectInsightFacetService projectInsightFacetService;

	ProjectInsightFacetController(ProjectInsightFacetService projectInsightFacetService) {
		this.projectInsightFacetService = projectInsightFacetService;
	}

	@RequestMapping(value = "/saveProjectInsightFacetCategoryList", method = RequestMethod.POST)
	public ServiceResponse saveProjectInsightFacetCategoryList(
			@RequestBody List<ProjectInsightFacetCategoryDTO> projectInsightFacetCategoryDTOList) {
		return projectInsightFacetService.saveProjectInsightFacetCategoryList(projectInsightFacetCategoryDTOList);
	}

	@RequestMapping(value = "/saveProjectInsightFacetCategory", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> saveProjectInsightFacetCategory(
			@RequestBody List<ProjectInsightFacetCategoryDTO> projectInsightFacetCategoryDTOList) {
		return ResponseEntity
				.ok(projectInsightFacetService.saveProjectInsightFacetCategoryList(projectInsightFacetCategoryDTOList));
	}

	@GetMapping("/getAllProjectInsightFacetCategory")
	public ResponseEntity<List<ProjectInsightFacetCategory>> getAllProjectInsightFacetCategory() {
		return ResponseEntity.ok(projectInsightFacetService.getAllProjectInsightFacetCategory());
	}

	@GetMapping("/getProjectInsightCategoryById/{id}")
	public ResponseEntity<ProjectInsightFacetCategoryDTO> getProjectInsightCategoryById(@PathVariable Long id) {
		return ResponseEntity.ok(projectInsightFacetService.getProjectInsightCategoryById(id));
	}

	@GetMapping("/deleteProjectInsightCategoryById/{id}")
	public ResponseEntity<ServiceResponse> deleteProjectInsightCategoryById(@PathVariable Long id) {
		return ResponseEntity.ok(projectInsightFacetService.deleteProjectInsightCategoryById(id));
	}

}
