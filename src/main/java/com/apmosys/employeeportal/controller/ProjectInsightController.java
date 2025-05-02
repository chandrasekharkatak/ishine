package com.apmosys.employeeportal.controller;

import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.EmployeeDocumentDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
import com.apmosys.employeeportal.dto.ProjectInsightFilterDTO;
import com.apmosys.employeeportal.dto.ProjectInsightUserContributionDTO;
import com.apmosys.employeeportal.response.SearchResultResponse;
import com.apmosys.employeeportal.service.ProjectInsightService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ProjectInsightController {
	
	@Autowired
	ProjectInsightService projectInsightService;
	
	@RequestMapping(value = "/createProjectInsightQuestion", method = RequestMethod.POST)
	public ServiceResponse createProjectInsightQuestion(@RequestBody ProjectInsightDTO projectInsightDTO) {

		ServiceResponse response = projectInsightService.createProjectInsightQuestion(projectInsightDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllProjectInsightList", method = RequestMethod.POST)
	public ServiceResponse getAllProjectInsightList(@RequestBody ProjectInsightDTO projectInsightDTO) {

		ServiceResponse response = projectInsightService.getAllProjectInsightList(projectInsightDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllQuestionsByProjectId", method = RequestMethod.POST)
	public ServiceResponse getAllQuestionsBySurveyId(@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.getAllQuestionsByProjectId(projectInsightDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllProjectInsightQuestionsByProjectIdAndEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllProjectInsightQuestionsByProjectIdAndEmpId(@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.getAllProjectInsightQuestionsByProjectIdAndEmpId(projectInsightDTO);
		return response;
	}
	
	@RequestMapping(value = "/getUserUploadedFileForQuestion", method = RequestMethod.POST)
	public ServiceResponse getUserUploadedFileForQuestion(@RequestBody EmployeeDocumentDTO employeeDocumentDTO) {
		ServiceResponse response = projectInsightService.getUserUploadedFileForQuestion(employeeDocumentDTO);
		return response;
	}
	
	@RequestMapping(value = "/updateProjectInsightQuestion", method = RequestMethod.POST)
	public ServiceResponse updateProjectInsightQuestion(@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.updateProjectInsightQuestion(projectInsightDTO);
		return response;
	}
	
	@RequestMapping(value = "/getAllProjectInsightResponsesByProjectId", method = RequestMethod.POST)
	public ServiceResponse getAllProjectInsightResponsesByProjectId(@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.getAllProjectInsightResponsesByProjectId(projectInsightDTO);
		return response;
	}
	
	@RequestMapping(value = "/saveProjectInsightResponse", method = RequestMethod.POST,consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ServiceResponse saveProjectInsightResponse( @RequestPart("projectInsightDTO") ProjectInsightDTO projectInsightDTO,@RequestPart(value = "files", required = false) List<MultipartFile> files) {
		ServiceResponse response = projectInsightService.saveProjectInsightResponse(projectInsightDTO,files);
		return response;
	}

	@RequestMapping(value = "/getAllProjectInsightContributionList", method = RequestMethod.POST)
	public ServiceResponse getAllProjectInsightContributionList(@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.getAllProjectInsightContributionList(projectInsightDTO);
		return response;
	}
	
	@RequestMapping(value = "/saveReviewPoints", method = RequestMethod.POST,consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ServiceResponse saveReviewPoints( @RequestPart("projectInsightDTO") ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.saveReviewPoints(projectInsightDTO);
		return response;
	}
	
    /*
     * Search Apis
     * */
	
	@RequestMapping(value = "/onSearchTerm", method = RequestMethod.GET)
	public ResponseEntity<SearchResultResponse> onSearchTerm(@RequestParam String search) {
		return projectInsightService.onSearchTerm(search);
	}
	
	@RequestMapping(value = "/suggestSearchOption", method = RequestMethod.GET)
	public ResponseEntity<Set<String>> suggestSearchOption(@RequestParam String search) {
		return projectInsightService.suggestSearchOption(search);
	}
	
	@RequestMapping(value = "/getFilterList", method = RequestMethod.GET)
	public ResponseEntity<List<ProjectInsightFilterDTO>> getFilterList() {
		return projectInsightService.getFilterList();
	}
	
	/*
     * User contribution Apis
     * */
	
	@RequestMapping(value = "/getContibutionByEmpId", method = RequestMethod.POST)
	public ResponseEntity<List<ProjectInsightUserContributionDTO>> getContibutionByEmpId(@RequestBody ProjectInsightUserContributionDTO projectInsightUserContributionDTO){
		return projectInsightService.getContributionByEmpId(projectInsightUserContributionDTO);
	}
	
	@RequestMapping(value = "/createUserContribution", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ServiceResponse> createUserContribution(
	    @RequestPart("userContribution") ProjectInsightUserContributionDTO projectInsightUserContributionDTO,
	    @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
	    
	    if (attachments != null && !attachments.isEmpty()) {
	        projectInsightUserContributionDTO.setAttachments(attachments);
	    }
	    
	    return projectInsightService.createOrUpdateUserContribution(projectInsightUserContributionDTO);
	}
	
	@RequestMapping(value = "/getUserContributionForReview", method = RequestMethod.POST)
	public ResponseEntity<List<ProjectInsightUserContributionDTO>> getUserContributionForReview(@RequestBody ProjectInsightUserContributionDTO projectInsightUserContributionDTO){
		return projectInsightService.getUserContributionForReview(projectInsightUserContributionDTO);
	}
	
	@RequestMapping(value = "/processUserContribution", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> processUserContribution(@RequestBody ProjectInsightUserContributionDTO projectInsightUserContributionDTO){
		return projectInsightService.processUserContribution(projectInsightUserContributionDTO);
	}
}
