package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.EmployeeDocumentDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
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
}
