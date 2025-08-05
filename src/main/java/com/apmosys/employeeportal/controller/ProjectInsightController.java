package com.apmosys.employeeportal.controller;

import java.util.List;
import java.util.Set;

import javax.persistence.OptimisticLockException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.EmployeeDocumentDTO;
import com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
import com.apmosys.employeeportal.dto.ProjectInsightFilterDTO;
import com.apmosys.employeeportal.dto.ProjectInsightUserContributionDTO;
import com.apmosys.employeeportal.mongodb.dto.ProjectInsightDetailsDTO;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightStructure;
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
	public ServiceResponse getAllProjectInsightQuestionsByProjectIdAndEmpId(
			@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService
				.getAllProjectInsightQuestionsByProjectIdAndEmpId(projectInsightDTO);
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

	@RequestMapping(value = "/saveProjectInsightResponse", method = RequestMethod.POST, consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE })
	public ServiceResponse saveProjectInsightResponse(
			@RequestPart("projectInsightDTO") ProjectInsightDTO projectInsightDTO,
			@RequestPart(value = "files", required = false) List<MultipartFile> files) {
		ServiceResponse response = projectInsightService.saveProjectInsightResponse(projectInsightDTO, files);
		return response;
	}

	@RequestMapping(value = "/getAllProjectInsightContributionList", method = RequestMethod.POST)
	public ServiceResponse getAllProjectInsightContributionList(@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.getAllProjectInsightContributionList(projectInsightDTO);
		return response;
	}

	@RequestMapping(value = "/saveReviewPoints", method = RequestMethod.POST, consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE })
	public ServiceResponse saveReviewPoints(@RequestPart("projectInsightDTO") ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.saveReviewPoints(projectInsightDTO);
		return response;
	}

	/*
	 * Search Apis
	 */

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
	 */

	@RequestMapping(value = "/getContibutionByEmpId", method = RequestMethod.POST)
	public ResponseEntity<List<ProjectInsightUserContributionDTO>> getContibutionByEmpId(
			@RequestBody ProjectInsightUserContributionDTO projectInsightUserContributionDTO) {
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
	public ResponseEntity<List<ProjectInsightUserContributionDTO>> getUserContributionForReview(
			@RequestBody ProjectInsightUserContributionDTO projectInsightUserContributionDTO) {
		return projectInsightService.getUserContributionForReview(projectInsightUserContributionDTO);
	}

	@RequestMapping(value = "/processUserContribution", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> processUserContribution(
			@RequestBody ProjectInsightUserContributionDTO projectInsightUserContributionDTO) {
		return projectInsightService.processUserContribution(projectInsightUserContributionDTO);
	}

	/*
	 * New Implimentation Project Insight : MongoDB ----------- [START]
	 * --------------------------------------
	 */

	@RequestMapping(value = "/onSaveAsDraft", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> onSaveAsDraft(@RequestBody ProjectInsightStructure projectInsightStructure) {
	    try {
	        ProjectInsightStructure saved = projectInsightService.saveAsDraft(projectInsightStructure);

	        ServiceResponse response = new ServiceResponse();
	        response.setServiceStatus("Successfully Saved Project in Draft");
	        response.setServiceResponse(saved);
	        return ResponseEntity.ok(response);
	    } catch (OptimisticLockingFailureException | OptimisticLockException e) {
	        ServiceResponse response = new ServiceResponse();
	        response.setServiceStatus("CONFLICT");
	        response.setServiceResponse("Version conflict: someone else has modified the project. Please reload and try again.");
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	    } catch (Exception e) {
	        e.printStackTrace();
	        ServiceResponse response = new ServiceResponse();
	        response.setServiceStatus("ERROR");
	        response.setServiceResponse("An unexpected error occurred while saving draft: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	@RequestMapping(value = "/onSaveAndAssign", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> saveAndAssign(@RequestBody ProjectInsightStructure projectInsightStructure) {
	    try {
	        ProjectInsightStructure saved = projectInsightService.saveAndAssign(projectInsightStructure);

	        ServiceResponse response = new ServiceResponse();
	        response.setServiceStatus("Project Saved Successfully");
	        response.setServiceResponse(saved);
	        return ResponseEntity.ok(response);
	    } catch (OptimisticLockingFailureException | OptimisticLockException e) {
	        ServiceResponse response = new ServiceResponse();
	        response.setServiceStatus("Version conflict: someone else has modified the project. Please reload and try again.");
	        response.setServiceResponse(null);
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	    } catch (Exception e) {
	        ServiceResponse response = new ServiceResponse();
	        response.setServiceStatus("An unexpected error occurred while saving the project.");
	        response.setServiceResponse(null);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}


	@RequestMapping(value = "/getAllProjectInsight", method = RequestMethod.GET)
	public ResponseEntity<List<ProjectInsighProjectMappingDTO>> getAllProjectInsight(@RequestParam(required=false) String domain) {
		List<ProjectInsighProjectMappingDTO> list = projectInsightService.getAllProjectInsight(domain);
		return ResponseEntity.ok(list);
	}

	@RequestMapping(value = "/getProjectInsightByInsightId/{id}", method = RequestMethod.GET)
	public ResponseEntity<ProjectInsightStructure> getProjectInsightByInsightId(@PathVariable String id) {
		ProjectInsightStructure response = projectInsightService.getProjectInsightByInsightId(id);
		return ResponseEntity.ok(response);
	}

	@RequestMapping(value = "/deleteProjectInsightById/{id}", method = RequestMethod.GET)
	public ResponseEntity<ServiceResponse> deleteProjectInsightById(@PathVariable String id) {
		return projectInsightService.deleteProjectInsightById(id);
	}

	@RequestMapping(value = "/updateProjectInsightById/{id}", method = RequestMethod.POST)
	public ResponseEntity<ProjectInsightStructure> updateProjectInsightById(
			@PathVariable String id,
			@RequestBody ProjectInsightStructure updatedData) {

		ProjectInsightStructure updated = projectInsightService.updateProjectInsightById(id, updatedData);
		return ResponseEntity.ok(updated);
	}

	@RequestMapping(value = "/searchProjectInsight", method = RequestMethod.GET)
	public ResponseEntity<List<ProjectInsighProjectMappingDTO>> search(
			@RequestParam("q") String keyword) {
		List<ProjectInsighProjectMappingDTO> results = projectInsightService.searchProjectInsightStructures(keyword);
		return ResponseEntity.ok(results);
	}

	/*
	 * New Implimentation Project Insight : User Contribution ----------- [START]
	 * --------------------------------------
	 */

	@RequestMapping(value = "/getProjectInsightByAssignedToEmpId", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> getProjectInsightByAssignedToEmpId(
			@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.getProjectInsightByAssignedToEmpId(projectInsightDTO);
		return ResponseEntity.ok(response);
	}
	
	@RequestMapping(value = "/getReviewersForQuestion", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> getReviewersForQuestion(@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.getReviewersForQuestion(projectInsightDTO);
		return ResponseEntity.ok(response);
	}
	
	@RequestMapping(value = "/onSaveResponseAsDraft", method = RequestMethod.POST)
	public ResponseEntity<ServiceResponse> onSaveResponseAsDraft(@RequestBody ProjectInsightStructure userDraft,
	                                   @RequestParam String empId) {
		ServiceResponse response = projectInsightService.onSaveResponseAsDraft(userDraft, empId);
		return ResponseEntity.ok(response);
	}

	/*	
	 * MongoDb New Structure Implementation [START]
	 */

	@GetMapping(value = "/getProjectInsightDetailsByObjectId")
	public ResponseEntity<ProjectInsightDetailsDTO> getProjectInsightDetailsByObjectId(@RequestParam String id) {
		return ResponseEntity.ok(projectInsightService.getProjectInsightDetailsByObjectId(id));
	}

	@PostMapping(value = "/getProjectInsightGroupDetailsByObjectId")
	public ResponseEntity<ProjectInsightDetailsDTO> getProjectInsightGroupDetailsByObjectId(@RequestBody String id) {
		return ResponseEntity.ok(projectInsightService.getProjectInsightGroupDetailsByObjectId(id));
	}

	@PostMapping(value = "/getProjectInsightQuestionDetailsByObjectId")
	public ResponseEntity<ProjectInsightDetailsDTO> getProjectInsightQuestionDetailsByObjectId(@RequestBody String id) {
		return ResponseEntity.ok(projectInsightService.getProjectInsightQuestionDetailsByObjectId(id));
	}
	
	@GetMapping(value = "/getProjectInsightQuestionDetailsByParentIdAndParentType")
	public ResponseEntity<ServiceResponse> getProjectInsightQuestionDetailsByParentIdAndParentType(@RequestParam String parentId, @RequestParam String parentType) {
		return ResponseEntity.ok(projectInsightService.getProjectInsightQuestionDetailsByParentIdAndParentType(parentId,parentType));
	}
	
	@GetMapping(value = "/getProjectInsightGroupDetailsByParentIdAndParentType")
	public ResponseEntity<ServiceResponse> getProjectInsightGroupDetailsByParentIdAndParentType(@RequestParam String parentId, @RequestParam String parentType) {
		return ResponseEntity.ok(projectInsightService.getProjectInsightGroupDetailsByParentIdAndParentType(parentId,parentType));
	}

	@PostMapping(value = "/saveProjectInsightDetails")
	public ResponseEntity<ServiceResponse> saveProjectInsightDetails(@RequestBody ProjectInsightDetailsDTO projectInsightDetailsDTO) {
		return ResponseEntity.ok(projectInsightService.saveProjectInsightDetails(projectInsightDetailsDTO));
	}

	@PostMapping(value = "/saveProjectInsightGroupDetails")
	public ResponseEntity<ServiceResponse> saveProjectInsightGroupDetails(@RequestBody ProjectInsightDetailsDTO projectInsightDetailsDTO) {
		return ResponseEntity.ok(projectInsightService.saveProjectInsightGroupDetails(projectInsightDetailsDTO));
	}

	@PostMapping(value = "/saveProjectInsightQuestionDetails")
	public ResponseEntity<ServiceResponse> saveProjectInsightQuestionDetails(@RequestBody ProjectInsightQuestionDetails projectInsightQuestionDetails) {
		return ResponseEntity.ok(projectInsightService.saveProjectInsightQuestionDetails(projectInsightQuestionDetails));
	}


	

	


	/*	
	 * MongoDb New Structure Implementation [END]
	 */


}
