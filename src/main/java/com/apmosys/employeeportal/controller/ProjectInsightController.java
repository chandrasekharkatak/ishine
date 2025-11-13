package com.apmosys.employeeportal.controller;

import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ApprovalRequest;
import com.apmosys.employeeportal.dto.EmployeeDocumentDTO;
import com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
import com.apmosys.employeeportal.dto.ProjectInsightFilterDTO;
import com.apmosys.employeeportal.dto.ProjectInsightUserContributionDTO;
import com.apmosys.employeeportal.dto.ProjectQuestionStatusDto;
import com.apmosys.employeeportal.dto.ProjectSectionData;
import com.apmosys.employeeportal.dto.QuesAndResponseDto;
import com.apmosys.employeeportal.dto.QuestionGroupRequest;
import com.apmosys.employeeportal.dto.QuestionMappedStatusDto;
import com.apmosys.employeeportal.dto.RefreshRequestDto;
import com.apmosys.employeeportal.model.ProjectInsightDomainData;
import com.apmosys.employeeportal.mongodb.dto.ProjectInsightDetailsDTO;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightGroupDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetailsHistory;
import com.apmosys.employeeportal.response.SearchResultResponse;
import com.apmosys.employeeportal.service.ProjectInsightService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api")
public class ProjectInsightController {

	@Autowired
	ProjectInsightService projectInsightService;

	// @RequestBody AllPIQuestionRequest quesRequest
	@PostMapping(value = "/getAllQuestionsWith")
	public ResponseEntity<List<ProjectQuestionStatusDto>> getAllQuestionsStatusWise(
			@RequestBody QuestionGroupRequest request) throws Exception {
		List<ProjectQuestionStatusDto> response = projectInsightService.getAllQuestionsStatusWise(request.getEmpId());
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/getAllQuestionsForApprovalTab")
	public ResponseEntity<List<ProjectQuestionStatusDto>> getAllQuestionsForApproval(
			@RequestBody QuestionGroupRequest request) throws Exception {
		List<ProjectQuestionStatusDto> response = projectInsightService.getAllQuestionsApprovalWise(request.getEmpId());
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/getGroupStatusData")
	public ResponseEntity<ProjectInsightGroupDetails> getGroupsDetailsById(@RequestBody QuestionGroupRequest request) {
		ProjectInsightGroupDetails response = projectInsightService.getAllGroupsDataIn(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/getAllgroupstatusdata")
	public ResponseEntity<List<ProjectQuestionStatusDto>> getGroupsStatusDetails(
			@RequestBody QuestionGroupRequest request) {
		List<ProjectQuestionStatusDto> response = projectInsightService.getAllGroupsDataByParentIdAndParentType(
				request.getParentId(), request.getParentType(), request.getEmpId());
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/fetchApprovalCountData")
	public ResponseEntity<List<ProjectQuestionStatusDto>> getApprovalTabCountData(
			@RequestBody QuestionGroupRequest request) {
		List<ProjectQuestionStatusDto> response = projectInsightService
				.getAllApprovalTabCountData(request.getParentId(), request.getParentType(), request.getEmpId());
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/groupQuestionDetails")
	public ResponseEntity<QuesAndResponseDto> getQuestionData(@RequestBody QuestionGroupRequest request) {
		QuesAndResponseDto response = projectInsightService.getQuestionDataById(request.getParentId(),
				request.getEmpId());
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/allGroupQuestions")
	public ResponseEntity<QuestionMappedStatusDto> getAllQuestions(@RequestBody QuestionGroupRequest request) {
		QuestionMappedStatusDto response = projectInsightService.getAllQuestionsOfGroup(request.getParentId(),
				request.getParentType(), request.getEmpId());
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/allGroupApprovalQuestions")
	public ResponseEntity<QuestionMappedStatusDto> getAllApprovalQuestions(@RequestBody QuestionGroupRequest request)
			throws Exception {
		QuestionMappedStatusDto response = projectInsightService.getAllQuestionsforApprovalTab(request.getParentId(),
				request.getParentType(), request.getEmpId());
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/getAllResponseHistory")
	public ResponseEntity<List<ProjectInsightResponseDetailsHistory>> getAllResponseHistory(
			@RequestBody QuestionGroupRequest request) {
		return ResponseEntity.ok(projectInsightService.getResponseHistory(request.getParentId(), request.getEmpId()));
	}

	// Temperory Postman Api
	@GetMapping(value = "/groupQuesCount")
	public void getQuesCount() {
		projectInsightService.setDataInQuestions();
	}

	@PostMapping(value = "/saveAnswerDraft")
	public ResponseEntity<String> saveAnswerAsDraft(@RequestBody ProjectInsightResponseDetails response) {
		String answer = projectInsightService.saveResponseAsDraft(response);
		return ResponseEntity.ok(answer);
	}

	@PostMapping(value = "/saveApproval")
	public ResponseEntity<ProjectInsightResponseDetails> saveApproval(@RequestBody ApprovalRequest request)
			throws Exception {
		ProjectInsightResponseDetails answer = projectInsightService.saveApprovalForResponse(request);
		return ResponseEntity.ok(answer);
	}

	@PostMapping(value = "/cleanAndReassign")
	public ResponseEntity<String> cleanAndReassignUser(@RequestBody QuestionGroupRequest request) throws Exception {
		String answer = projectInsightService.cleanResponseAndReassign(request);
		return ResponseEntity.ok(answer);
	}

	@PostMapping(value = "/assignQuestionforReview")
	public ResponseEntity<String> assignQuestionsforReview(@RequestBody QuestionGroupRequest request) {
		String answer = projectInsightService.assignReviewersToAnsweredQuestions(request);
		return ResponseEntity.ok(answer);
	}

	@PostMapping(value = "/refreshStatusCount")
	public ResponseEntity<Map<String, ProjectQuestionStatusDto>> refreshQuestionStatusCount(
			@RequestBody RefreshRequestDto request) {
		Map<String, ProjectQuestionStatusDto> answer = projectInsightService.getStatusWiseCountByIds(request);
		return ResponseEntity.ok(answer);
	}

	@PostMapping(value = "/refreshApprovalTabCount")
	public ResponseEntity<Map<String, ProjectQuestionStatusDto>> refreshApprovalCount(
			@RequestBody RefreshRequestDto request) {
		Map<String, ProjectQuestionStatusDto> answer = projectInsightService.refreshCountsApproval(request);
		return ResponseEntity.ok(answer);
	}

	@PostMapping(value = "/getUserUploadedFileForQuestion")
	public ServiceResponse getUserUploadedFileForQuestion(@RequestBody EmployeeDocumentDTO employeeDocumentDTO) {
		ServiceResponse response = projectInsightService.getUserUploadedFileForQuestion(employeeDocumentDTO);
		return response;
	}

	@PostMapping(value = "/saveProjectInsightResponse", consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE })
	public ServiceResponse saveProjectInsightResponse(
			@RequestPart("projectInsightDTO") ProjectInsightDTO projectInsightDTO,
			@RequestPart(value = "files", required = false) List<MultipartFile> files) {
		ServiceResponse response = projectInsightService.saveProjectInsightResponse(projectInsightDTO, files);
		return response;
	}

	@PostMapping(value = "/saveReviewPoints", consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE })
	public ServiceResponse saveReviewPoints(@RequestPart("projectInsightDTO") ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.saveReviewPoints(projectInsightDTO);
		return response;
	}

	@GetMapping(value = "/onSearchTerm")
	public ResponseEntity<SearchResultResponse> onSearchTerm(@RequestParam String search) {
		return projectInsightService.onSearchTerm(search);
	}

	@GetMapping(value = "/suggestSearchOption")
	public ResponseEntity<Set<String>> suggestSearchOption(@RequestParam String search) {
		return projectInsightService.suggestSearchOption(search);
	}

	@GetMapping(value = "/getFilterList")
	public ResponseEntity<List<ProjectInsightFilterDTO>> getFilterList() {
		return projectInsightService.getFilterList();
	}

	@PostMapping(value = "/getContibutionByEmpId")
	public ResponseEntity<List<ProjectInsightUserContributionDTO>> getContibutionByEmpId(
			@RequestBody ProjectInsightUserContributionDTO projectInsightUserContributionDTO) {
		return projectInsightService.getContributionByEmpId(projectInsightUserContributionDTO);
	}

	@PostMapping(value = "/createUserContribution", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ServiceResponse> createUserContribution(
			@RequestPart("userContribution") ProjectInsightUserContributionDTO projectInsightUserContributionDTO,
			@RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
		if (attachments != null && !attachments.isEmpty()) {
			projectInsightUserContributionDTO.setAttachments(attachments);
		}
		return projectInsightService.createOrUpdateUserContribution(projectInsightUserContributionDTO);
	}

	@PostMapping(value = "/getUserContributionForReview")
	public ResponseEntity<List<ProjectInsightUserContributionDTO>> getUserContributionForReview(
			@RequestBody ProjectInsightUserContributionDTO projectInsightUserContributionDTO) {
		return projectInsightService.getUserContributionForReview(projectInsightUserContributionDTO);
	}

	@PostMapping(value = "/processUserContribution")
	public ResponseEntity<ServiceResponse> processUserContribution(
			@RequestBody ProjectInsightUserContributionDTO projectInsightUserContributionDTO) {
		return projectInsightService.processUserContribution(projectInsightUserContributionDTO);
	}

	@GetMapping(value = "/getAllProjectInsight")
	public ResponseEntity<List<ProjectInsighProjectMappingDTO>> getAllProjectInsight(
			@RequestParam(required = false) String domainName, @RequestParam(required = false) String unique_name,
			@RequestParam(required = false) String ids) {
		List<ProjectInsighProjectMappingDTO> list = projectInsightService.getAllProjectInsight(domainName, unique_name,
				ids);
		return ResponseEntity.ok(list);
	}

	@GetMapping(value = "/getDomainRecommendation")
	public ResponseEntity<List<String>> getDomainRecommendation(@RequestParam("query") String domainName) {
		List<String> list = projectInsightService.getDomainSearchRecommendation(domainName);
		return ResponseEntity.ok(list);
	}

	@GetMapping(value = "/searchDomain")
	public ResponseEntity<List<ProjectInsightDomainData>> searchDomain(@RequestParam("query") String keyword) {
		List<ProjectInsightDomainData> list = projectInsightService.searchProjectInsightDomainData(keyword);
		return ResponseEntity.ok(list);
	}

	@GetMapping(value = "/deleteProjectInsightById/{id}")
	public ResponseEntity<ServiceResponse> deleteProjectInsightById(@PathVariable String id) {
		return projectInsightService.deleteProjectInsightById(id);
	}

	/*
	 * New Implimentation Project Insight : User Contribution ----------- [START]
	 * --------------------------------------
	 */

	@PostMapping(value = "/getReviewersForQuestion")
	public ResponseEntity<ServiceResponse> getReviewersForQuestion(@RequestBody ProjectInsightDTO projectInsightDTO) {
		ServiceResponse response = projectInsightService.getReviewersForQuestion(projectInsightDTO);
		return ResponseEntity.ok(response);
	}

	// @PostMapping(value = "/onSaveResponseAsDraft")
	// public ResponseEntity<ServiceResponse> onSaveResponseAsDraft(@RequestBody
	// ProjectInsightStructure userDraft,
	// @RequestParam String empId) {
	// ServiceResponse response =
	// projectInsightService.onSaveResponseAsDraft(userDraft, empId);
	// return ResponseEntity.ok(response);
	// }

	/*
	 * MongoDb New Structure Implementation [START]
	 */

	@GetMapping(value = "/getProjectInsightDetailsByObjectId")
	public ResponseEntity<ProjectInsightDetailsDTO> getProjectInsightDetailsByObjectId(@RequestParam String id) {
		return ResponseEntity.ok(projectInsightService.getProjectInsightDetailsByObjectId(id));
	}

	@GetMapping(value = "/getAllProjectInsightGroupsByParentId")
	public ResponseEntity<ServiceResponse> getAllProjectInsightGroupsByParentId(@RequestParam String parentId,
			@RequestParam String parentType) {
		return ResponseEntity.ok(projectInsightService.getAllProjectInsightGroupsByParentId(parentId, parentType));
	}

	@GetMapping(value = "/getProjectInsightGroupDetailsByObjectId")
	public ResponseEntity<ProjectInsightDetailsDTO> getProjectInsightGroupDetailsByObjectId(@RequestParam String id) {
		return ResponseEntity.ok(projectInsightService.getProjectInsightGroupDetailsByObjectId(id));
	}

	@GetMapping(value = "/getProjectInsightQuestionDetailsByObjectId")
	public ResponseEntity<ServiceResponse> getProjectInsightQuestionDetailsByObjectId(@RequestParam String id) {
		return ResponseEntity.ok(projectInsightService.getProjectInsightQuestionDetailsByObjectId(id));
	}

	@GetMapping(value = "/getProjectInsightDetailsForExcelDownload")
	public ResponseEntity<ServiceResponse> getProjectInsightDetailsForExcelDownload(@RequestParam String id) {
		return ResponseEntity.ok(projectInsightService.getProjectInsightDetailsForExcelDownload(id));
	}

	@GetMapping(value = "/getProjectInsightQuestionDetailsByParentIdAndParentType")
	public ResponseEntity<ServiceResponse> getProjectInsightQuestionDetailsByParentIdAndParentType(
			@RequestParam String parentId, @RequestParam String parentType) {
		return ResponseEntity.ok(
				projectInsightService.getProjectInsightQuestionDetailsByParentIdAndParentType(parentId, parentType));
	}

	@GetMapping(value = "/getProjectInsightGroupDetailsByParentIdAndParentType")
	public ResponseEntity<ServiceResponse> getProjectInsightGroupDetailsByParentIdAndParentType(
			@RequestParam String parentId, @RequestParam String parentType) {
		return ResponseEntity
				.ok(projectInsightService.getProjectInsightGroupDetailsByParentIdAndParentType(parentId, parentType));
	}

	@org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
	@PostMapping(value = "/saveProjectInsightDetailsFromExcel")
	public ResponseEntity<ServiceResponse> saveProjectInsightDetailsFromExcel(
			@RequestBody ProjectSectionData projectSectionData) {
		return ResponseEntity.ok(projectInsightService.saveProjectInsightDetailsFromExcel(projectSectionData));
	}

	@PostMapping(value = "/saveProjectInsightDetails")
	public ResponseEntity<ServiceResponse> saveProjectInsightDetails(
			@RequestBody ProjectInsightDetailsDTO projectInsightDetailsDTO) {
		return ResponseEntity.ok(projectInsightService.saveProjectInsightDetails(projectInsightDetailsDTO));
	}

	@PostMapping(value = "/saveProjectInsightStaticGroupDetails")
	public ResponseEntity<ServiceResponse> saveProjectInsightStaticGroupDetails(
			@RequestBody ProjectInsightGroupDetails projectInsightGroupDetails) {
		return ResponseEntity
				.ok(projectInsightService.saveProjectInsightStaticGroupDetails(projectInsightGroupDetails));
	}

	@PostMapping(value = "/saveProjectInsightGroupDetails")
	public ResponseEntity<ServiceResponse> saveProjectInsightGroupDetails(
			@RequestBody ProjectInsightDetailsDTO projectInsightDetailsDTO) {
		return ResponseEntity.ok(projectInsightService.saveProjectInsightGroupDetails(projectInsightDetailsDTO));
	}

	@PostMapping(value = "/saveProjectInsightQuestionDetails")
	public ResponseEntity<ServiceResponse> saveProjectInsightQuestionDetails(
			@RequestBody ProjectInsightQuestionDetails projectInsightQuestionDetails) {
		return ResponseEntity
				.ok(projectInsightService.saveProjectInsightQuestionDetails(projectInsightQuestionDetails));
	}

	@PostMapping(value = "/deleteProjectInsightQuestionDetails")
	public ResponseEntity<ServiceResponse> saveProjectInsightGroupDetails(
			@RequestBody ProjectInsightQuestionDetails projectInsightQuestionDetails) {
		return ResponseEntity
				.ok(projectInsightService.deleteProjectInsightQuestionDetails(projectInsightQuestionDetails));
	}

	/*
	 * MongoDb New Structure Implementation [END]
	 */

}
